package ru.practicum.aggregator;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class EventSimilarityCalculator {
    // Карта: ID мероприятия → (ID пользователя → максимальный вес действия)
    final Map<Long, Map<Long, Double>> eventUserWeights = new ConcurrentHashMap<>();
    // Карта: ID мероприятия → сумма всех весов пользователей
    final Map<Long, Double> eventTotalWeights = new ConcurrentHashMap<>();
    // Карта: ID мероприятия A → (ID мероприятия B → сумма минимальных весов общих пользователей)
    final Map<Long, Map<Long, Double>> minWeightsSums = new ConcurrentHashMap<>();
    // Карта: ID пользователя → список ID мероприятий, с которыми он взаимодействовал
    final Map<Long, List<Long>> userEvents = new ConcurrentHashMap<>();

    public List<EventSimilarityAvro> updateWeights(long userId, long eventId, double newWeight) {
        if (!eventUserWeights.containsKey(eventId)) {
            return handleNewEvent(userId, eventId, newWeight);
        }

        Map<Long, Double> eventUsers = eventUserWeights.get(eventId);
        if (!eventUsers.containsKey(userId)) {
            return handleNewUserForEvent(userId, eventId, newWeight);
        }

        double oldWeight = eventUsers.get(userId);
        if (newWeight > oldWeight) {
            return handleWeightUpdate(userId, eventId, oldWeight, newWeight);
        }

        return Collections.emptyList();
    }

    /**
     *
     * @param userId
     * @param eventId
     * @param newWeight
     * @return
     * Создает новую запись для мероприятия в eventUserWeights
     *
     * Инициализирует общую сумму весов для мероприятия
     *
     * Добавляет мероприятие в список мероприятий пользователя
     *
     * Обновляет суммы минимумов для всех пар с новым мероприятием
     *
     * Рассчитывает и возвращает новые значения сходства
     */
    private List<EventSimilarityAvro> handleNewEvent(long userId, long eventId, double newWeight) {
        // Создаем новую запись для мероприятия
        Map<Long, Double> newMap = new ConcurrentHashMap<>();
        newMap.put(userId, newWeight);
        eventUserWeights.put(eventId, newMap);
        eventTotalWeights.put(eventId, newWeight);
        userEvents.compute(userId, (k, events) -> {
            if (events == null) {
                events = new ArrayList<>();
            }
            events.add(eventId);
            return events;
        });
        updateMinWeightsForUser(userId, eventId, 0.0, newWeight);
        return generateSimilaritiesForUserEvents(userId, eventId);
    }
    // Первое взаимодействие пользователя с этим мероприятием
    private List<EventSimilarityAvro> handleNewUserForEvent(long userId, long eventId, double newWeight) {
       eventUserWeights.get(eventId).put(userId, newWeight);
       eventTotalWeights.merge(eventId, newWeight, Double::sum);
       userEvents.compute(userId, (k, events) -> {
            if (events == null) {
                events = new ArrayList<>();
            }
            events.add(eventId);
            return events;
        });

        updateMinWeightsForUser(userId, eventId, 0.0, newWeight);
        return generateSimilaritiesForUserEvents(userId, eventId);
    }

    private List<EventSimilarityAvro> handleWeightUpdate(long userId, long eventId, double oldWeight, double newWeight) {
        eventUserWeights.get(eventId).put(userId, newWeight);

        double delta = newWeight - oldWeight;
        eventTotalWeights.merge(eventId, delta, Double::sum);
        updateMinWeightsForUser(userId, eventId, oldWeight, newWeight);
        return generateSimilaritiesForUserEvents(userId, eventId);
    }

    /**
     *
     * @param userId
     * @param eventId
     * @param oldWeight
     * @param newWeight
     * Когда вес пользователя для мероприятия изменяется, это может повлиять на минимальные веса во всех парах, где участвует это мероприятие и данный пользователь.
     *
     * Например, если у пользователя было:
     *
     * Вес для мероприятия A: 0.5 (старый)
     *
     * Вес для мероприятия B: 0.7
     *
     * Минимум был min(0.5, 0.7) = 0.5
     *
     * Если вес для A изменился на 0.8:
     *
     * Новый минимум min(0.8, 0.7) = 0.7
     *
     * Разница: 0.7 - 0.5 = 0.2
     *
     * Значит, общая сумма минимумов для пары (A,B) должна увеличиться на 0.2
     */
    private void updateMinWeightsForUser(long userId, long eventId, double oldWeight, double newWeight) {
        // список всех мероприятий, с которыми взаимодействовал пользователь
        List<Long> userEventIds = userEvents.get(userId);
        if (userEventIds == null) return;

        for (Long otherEventId : userEventIds) {
            if (otherEventId.equals(eventId)) continue;
            double otherWeight = eventUserWeights.get(otherEventId).get(userId);
           double oldMin = Math.min(oldWeight, otherWeight);
           double newMin = Math.min(newWeight, otherWeight);
            double delta = newMin - oldMin;

            if (delta != 0) {
                updateMinWeightsSum(eventId, otherEventId, delta);
            }
        }
    }

    private List<EventSimilarityAvro> generateSimilaritiesForUserEvents(long userId, long eventId) {
        List<EventSimilarityAvro> similarities = new ArrayList<>();
        List<Long> userEventIds = userEvents.get(userId);
        if (userEventIds == null) return similarities;

        for (Long otherEventId : userEventIds) {
            if (otherEventId.equals(eventId)) continue;

            double numerator = getMinWeightsSum(eventId, otherEventId);
            double denominator = Math.sqrt(eventTotalWeights.get(eventId)) * Math.sqrt(eventTotalWeights.get(otherEventId));

            if (denominator == 0) {
                continue;
            }

            double similarity = numerator / denominator;

            long firstEventId = Math.min(eventId, otherEventId);
            long secondEventId = Math.max(eventId, otherEventId);

            similarities.add(EventSimilarityAvro.newBuilder()
                    .setEventA(firstEventId)
                    .setEventB(secondEventId)
                    .setScore(similarity)
                    .setTimestamp(Instant.now())
                    .build());
        }

        similarities.sort(Comparator
                .comparing(EventSimilarityAvro::getEventA)
                .thenComparing(EventSimilarityAvro::getEventB));

        return similarities;
    }

    /**
     *
     * @param eventA
     * @param eventB
     * @param delta
     * Этот метод обновляет сумму минимальных весов (S_min) для пары мероприятий
        */
    private void updateMinWeightsSum(long eventA, long eventB, double delta) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeightsSums.compute(first, (k, innerMap) -> {
           if (innerMap == null) innerMap = new ConcurrentHashMap<>();
           innerMap.merge(second, delta, Double::sum);
            return innerMap;
        });
    }

    private double getMinWeightsSum(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minWeightsSums.getOrDefault(first, Collections.emptyMap())
                .getOrDefault(second, 0.0);
    }
}
