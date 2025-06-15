package ru.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.RecommendedEvent;
import ru.practicum.mapper.RecommendationMapper;
import ru.practicum.model.EventSimilarity;
import ru.practicum.model.UserAction;
import ru.practicum.repository.EventSimilarityRepository;
import ru.practicum.repository.UserActionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {
    static final double VIEW_WEIGHT = 0.4;
    static final double REGISTER_WEIGHT = 0.8;
    static final double LIKE_WEIGHT = 1.0;
    final UserActionRepository userActionRepository;
    final EventSimilarityRepository eventSimilarityRepository;
    final RecommendationMapper recommendationMapper;

    @Override
    public List<RecommendedEvent> getRecommendationsForUser(long userId, int maxResults) {
        log.info("Получение рекомендаций for userId={}, limit={}", userId, maxResults);

        //последние действия пользователя
        List<UserAction> userActions = userActionRepository.getRecentUserActions(userId, maxResults);

        if (userActions.isEmpty()) {
            return Collections.emptyList();
        }
        // Извлекаем ID событий, с которыми уже взаимодействовал пользователь
        Set<Long> userEventIds = extractUserEventIds(userActions);
        // Находим похожие события для действий пользователя
        List<EventSimilarity> similarEvents = findSimilarEventsForUser(userId, userActions);
        return similarEvents.stream()
                // Исключаем события, с которыми пользователь уже взаимодействовал
                .filter(e -> !userEventIds.contains(e.getEventIdB()))
               // Ограничиваем количество результатов
                .limit(maxResults)
                // Преобразуем в RecommendedEvent
                .map(this::convertToRecommendedEvent)
                .collect(Collectors.toList());
    }

    /**
     * Находит похожие события для указанного события
     *
     * @param eventId ID события
     * @param userId  ID пользователя
     * @return список похожих событий
     */
    @Override
    public List<RecommendedEvent> getSimilarEvents(long userId, long eventId) {
        log.info("Поиск  similarEvents for eventId={} и userId {}", eventId, userId);

        return eventSimilarityRepository.findUnseenSimilarEventsForUser(userId, eventId).stream()
                .map(this::convertToRecommendedEvent)
                .collect(Collectors.toList());
    }

    /**
     * Вычисляет веса взаимодействий для списка событий
     *
     * @param eventIds список ID событий
     * @return список событий с весами взаимодействий
     */
    @Override
    public List<RecommendedEvent> getInteractionsCount(List<Long> eventIds) {
        log.info("Получение макс.весов действий для списка events={}", eventIds);
        List<UserAction> userActions = userActionRepository.getByEventIds(eventIds);
        Map<Long, Map<Long, Double>> maxWeights = calculateMaxWeights(userActions);
        return getRecommendedEvent(maxWeights);
    }

    /**
     * Преобразует структуру максимальных весов действий пользователей по событиям в список рекомендованных событий.
     *
     * @param maxWeights вложенная Map, где:
     *                   - ключ верхнего уровня — идентификатор события (eventId),
     *                   - значение — Map с ключом userId и значением максимального веса действия пользователя для события.
     * @return список объектов {@link RecommendedEvent}, каждый из которых содержит идентификатор события и суммарный вес всех пользователей для этого события.
     * <p>
     * Метод суммирует максимальные веса всех пользователей для каждого события и формирует итоговый список рекомендаций.
     */
    private List<RecommendedEvent> getRecommendedEvent(Map<Long, Map<Long, Double>> maxWeights) {
        return maxWeights.entrySet().stream()
                .map(entry -> {
                    long eventId = entry.getKey();
                    // Суммируем все веса действий пользователей для этого события
                    double scoreSum = entry.getValue().values().stream()
                            .mapToDouble(Double::doubleValue)
                            .sum();
                    //объект RecommendedEvent с eventId и суммарным весом
                    return new RecommendedEvent(eventId, scoreSum);
                })
                .collect(Collectors.toList());
    }

    /**
     * Вычисляет максимальный вес действий каждого пользователя по событиям.
     *
     * @param userActions список действий пользователей, где каждое действие связано с событием и пользователем
     * @return вложенную Map, где:
     * - ключ верхнего уровня — идентификатор события (eventId)
     * - значение верхнего уровня — Map с ключом userId и значением максимального веса действия этого пользователя для события
     * <p>
     * Метод для каждого события и пользователя сохраняет максимальный вес действия,
     * если один пользователь совершал несколько действий для одного события, то берется максимальный вес.
     */
    private Map<Long, Map<Long, Double>> calculateMaxWeights(List<UserAction> userActions) {
        Map<Long, Map<Long, Double>> maxWeights = new HashMap<>();

        for (UserAction ua : userActions) {
            long eventId = ua.getEventId();
            long userId = ua.getUserId();
            double weight = calculateActionWeight(ua);

            Map<Long, Double> userMap = maxWeights.computeIfAbsent(eventId, e -> new HashMap<>());
            userMap.merge(userId, weight, Double::max);
        }
        return maxWeights;
    }

    /**
     * Находит похожие события для списка действий пользователя
     *
     * @param userActions список действий пользователя
     * @return список похожих событий
     */
    private List<EventSimilarity> findSimilarEventsForUser(long userId, List<UserAction> userActions) {
        return userActions.stream()
                .flatMap(action ->
                        eventSimilarityRepository.findUnseenSimilarEventsForUser(userId, action.getEventId()).stream()
                )
                .collect(Collectors.toList());
    }

    /**
     * Извлекает ID событий из списка действий пользователя
     *
     * @param userActions список действий пользователя
     * @return множество ID событий
     */
    private Set<Long> extractUserEventIds(List<UserAction> userActions) {
        return userActions.stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());
    }

    /**
     * Преобразует EventSimilarity в RecommendedEvent
     *
     * @param similarity объект с информацией о похожем событии
     * @return RecommendedEvent
     */
    private RecommendedEvent convertToRecommendedEvent(EventSimilarity similarity) {
        return new RecommendedEvent(
                similarity.getEventIdB(),
                similarity.getScore()
        );
    }

    private double calculateActionWeight(UserAction action) {
        return switch (action.getActionType()) {
            case VIEW -> VIEW_WEIGHT;
            case REGISTER -> REGISTER_WEIGHT;
            case LIKE -> LIKE_WEIGHT;
        };
    }
}
