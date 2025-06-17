package ru.practicum.aggregator;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.producer.KafkaProducerService;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class AggregatorService {

    final AggregationProcessor processor;
    final KafkaConsumer<String, SpecificRecordBase> consumer;
    final KafkaProducerService kafkaProducer;

    @Value("${kafka.topics.user-actions}")
    String inputTopic;

    @Value("${kafka.topics.events-similarity}")
    String outputTopic;

    @Value("${kafka.consumer.poll_timeout}")
    long pollTimeout;

    final ExecutorService executor = Executors.newSingleThreadExecutor();
    volatile boolean running = true;

    @PostConstruct
    public void start() {
        log.info("Подписка consumer на топик {}", inputTopic);
        consumer.subscribe(List.of(inputTopic));
        executor.submit(this::processMessages);
    }

    @PreDestroy
    public void stop() {
        running = false;
        consumer.wakeup();
        kafkaProducer.flush();
        executor.shutdown();
    }

    private void processMessages() {
        while (running) {
            try {
                var records = consumer.poll(Duration.ofMillis(pollTimeout));
                for (var record : records) {
                    if (record.value() instanceof UserActionAvro userAction) {
                        List<EventSimilarityAvro> result = processor.process(userAction);
                        result.forEach(sim -> kafkaProducer.send(sim, outputTopic));
                    }
                }
                if (!records.isEmpty()) consumer.commitSync();
                kafkaProducer.flush();
            } catch (WakeupException e) {
                if (!running) break;
            } catch (Exception e) {
                log.error("Ошибка в цикле обработки", e);
            }
        }
    }
}

