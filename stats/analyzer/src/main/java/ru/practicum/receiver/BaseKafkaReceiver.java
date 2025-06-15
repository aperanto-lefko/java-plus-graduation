package ru.practicum.receiver;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseKafkaReceiver<T> {
    @Value("${kafka.consumer.poll_timeout}")
    protected long pollTimeout;

    protected final KafkaConsumer<String, T> consumer;
    protected final ExecutorService executor = Executors.newSingleThreadExecutor();
    protected volatile boolean running = true;

    protected BaseKafkaReceiver(KafkaConsumer<String, T> consumer) {
        this.consumer = consumer;
    }

    protected abstract String getInputTopic();
    protected abstract void processMessage(T message);

    @PostConstruct
    public void start() {
        log.info("Подписка consumer на топик {}", getInputTopic());
        consumer.subscribe(Collections.singletonList(getInputTopic()));
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
                ConsumerRecords<String, T> records = consumer.poll(Duration.ofMillis(pollTimeout));
                log.info("Получено {} записей для обработки", records.count());

                for (ConsumerRecord<String, T> record : records) {
                    try {
                        T message = record.value();
                        log.info("Обработка сообщения: {}", message);
                        processMessage(message);
                    } catch (Exception e) {
                        log.error("Ошибка обработки сообщения: {}", record.value(), e);
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
