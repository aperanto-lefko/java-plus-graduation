package ru.practicum.aggregator;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.producer.KafkaProducerService;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AggregatorService {
    final KafkaConsumer<Void, SpecificRecordBase> consumer;
    final KafkaProducerService kafkaProducer;
    @Value("${kafka.topics.user-actions}")
    private String inputTopic;
    @Value("${kafka.topics.events-similarity}")
    private String outputTopic;
    @Value("${kafka.consumer.poll_timeout}")
    private long pollTimeout;
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    volatile boolean running = true;

    // Карта: ID мероприятия → (ID пользователя → вес действия)
    private final Map<Long, Map<Long, Double>> eventUserWeights = new ConcurrentHashMap<>();
    // Карта: ID мероприятия → сумма всех весов пользователей
    private final Map<Long, Double> eventTotalWeights = new ConcurrentHashMap<>();
    // Карта: ID мероприятия A → (ID мероприятия B → сумма минимальных весов общих пользователей)
    private final Map<Long, Map<Long, Double>> minWeightsSums = new ConcurrentHashMap<>();

    @PostConstruct
    public void start() {
        consumer.subscribe(Collections.singletonList(inputTopic));
        executor.submit(this::processMessages);
    }

    @PreDestroy
    public void stop() {
        running = false;
        consumer.wakeup();
        executor.shutdown();
        log.info("Kafka consumer остановлен");
    }
    private void processMessages() {
        while (running) {
            try {
                ConsumerRecords<Void, SpecificRecordBase> records = consumer.poll(Duration.ofMillis(pollTimeout));
                for (ConsumerRecord<Void, SpecificRecordBase> record : records) {
                    SpecificRecordBase value = record.value();
                    if (value instanceof UserActionAvro userAction) {
                        log.debug("Обработка действия: {}", userAction);
                        try {
                            processUserAction(userAction);
                        } catch (Exception e) {
                            log.error("Ошибка обработки действия: {}", userAction, e);
                        }
                    } else {
                        log.warn("Получено сообщение неизвестного типа: {}", value.getClass().getSimpleName());
                    }
                }
                if (!records.isEmpty()) {
                    consumer.commitSync();
                }
            } catch (WakeupException e) {
                if (!running) break;
            } catch (Exception e) {
                log.error("Ошибка в основном цикле обработки", e);
            }
        }
    }

    // Обработка  действия пользователя
    private void processUserAction(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();
    // Получаем вес действия
        double newWeight = getActionWeight(action.getActionType());
        // Обновляем вес пользователя для этого мероприятия, получаем старый вес
        double oldWeight = updateUserWeights(eventId, userId, newWeight);

        // Если вес увеличился,пересчитать сходства
        if (newWeight > oldWeight) {
            recalculateSimilarities(eventId, userId, oldWeight, newWeight);
        }

    }
    // Обновляет вес действия пользователя по мероприятию
    private double updateUserWeights(long eventId, long userId, double newWeight) {
        return eventUserWeights.compute(eventId, (k, userWeights) -> {
            if (userWeights == null) {
                userWeights = new ConcurrentHashMap<>(); // если это первое действие для события
            }
            Double currentWeight = userWeights.get(userId); // старый вес
            // если веса не было или он меньше нового
            if (currentWeight == null || newWeight > currentWeight) {
                // на сколько изменилась сумма весов мероприятия
                double delta = currentWeight == null ? newWeight : newWeight - currentWeight;

                // обновляем общую сумму весов по мероприятию
                eventTotalWeights.merge(eventId, delta, Double::sum);

                // сохраняем новый (максимальный) вес
                userWeights.put(userId, newWeight);
            }
            return userWeights;
        }).get(userId); // возвращаем новый (или старый) вес
    }
    private void recalculateSimilarities(long updatedEventId, long userId, double oldWeight, double newWeight) {
        // Перебираем все мероприятия (пары)
        eventUserWeights.forEach((otherEventId, otherUsers) -> {
            if (otherEventId.equals(updatedEventId)) return; // не сравниваем мероприятие само с собой

            Double otherWeight = otherUsers.get(userId); // вес этого же пользователя по другому мероприятию

            if (otherWeight == null) return; // пользователь не участвовал — не пересчитываем

            // старая и новая минимальная величина между этими двумя весами
            double oldMin = Math.min(oldWeight, otherWeight);
            double newMin = Math.min(newWeight, otherWeight);
            double deltaMin = newMin - oldMin;

            // если минимум изменился — надо обновить сумму минимумов
            if (deltaMin != 0) {
                updateMinWeightsSum(updatedEventId, otherEventId, deltaMin);
                sendSimilarity(updatedEventId, otherEventId); // и отправить новый результат
            }
        });
    }
    // Обновляет сумму минимумов для пары мероприятий
    private void updateMinWeightsSum(long eventA, long eventB, double delta) {
        // Сортировка по ID, чтобы сохранить симметричность (eventA < eventB)
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        // Атомарно обновляем внутреннюю карту
        minWeightsSums.compute(first, (k, innerMap) -> {
            if (innerMap == null) innerMap = new ConcurrentHashMap<>();
            innerMap.merge(second, delta, Double::sum); // прибавляем delta
            return innerMap;
        });
    }
    // Отправляет сообщение в Kafka о сходстве между мероприятиями
    private void sendSimilarity(long eventA, long eventB) {
        // Сортировка ID (для симметрии)
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        // Получаем нужные суммы
        Double sumMin = minWeightsSums.getOrDefault(first, Map.of()).get(second); // сумма минимумов общих пользователей
        Double sumA = eventTotalWeights.get(eventA); // общая сумма весов по первому мероприятию
        Double sumB = eventTotalWeights.get(eventB); // по второму

        // Проверка, что данные существуют и не нули
        if (sumMin == null || sumA == null || sumB == null || sumA == 0 || sumB == 0) {
            return; // нельзя посчитать сходство
        }

        // Формула косинусного сходства: (∑min) / (||A|| * ||B||)
        double similarity = sumMin / (Math.sqrt(sumA) * Math.sqrt(sumB));

        // Создание Avro-сообщения
        EventSimilarityAvro similarityEvent = EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore(similarity)
                .setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()))
                .build();

        // Отправка в Kafka
        kafkaProducer.send(similarityEvent, outputTopic);
        log.debug("Отправлено сходство: {} <-> {} = {}", first, second, similarity);
    }
            private double getActionWeight(ActionTypeAvro actionType) {
                return switch (actionType) {
                    case VIEW -> 0.4;
                    case REGISTER -> 0.8;
                    case LIKE -> 1.0;
                };
            }
}
