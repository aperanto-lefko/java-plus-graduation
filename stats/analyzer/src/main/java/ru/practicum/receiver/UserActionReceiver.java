package ru.practicum.receiver;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.UserActionService;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Slf4j
@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserActionReceiver {

    @Value("${kafka.topics.user-actions}")
    private String inputTopic;

    @Value("${kafka.consumer.poll_timeout}")
    private long pollTimeout;

    final KafkaConsumer<String, UserActionAvro> consumer;
    final UserActionService userActionService;
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    volatile boolean running = true;


    @PostConstruct
    public void start() {
        log.info("Подписка consumer на топик {}", inputTopic);
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
                ConsumerRecords<String, UserActionAvro> records = consumer.poll(Duration.ofMillis(pollTimeout));
                log.info("Получено {} записей для обработки", records.count());
                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    try {
                        UserActionAvro userAction = record.value();
                        log.info("Обработка действия: {}", userAction);
                        userActionService.process(userAction);
                    } catch (Exception e) {
                        log.error("Ошибка обработки действия: {}", record.value(), e);
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
}
