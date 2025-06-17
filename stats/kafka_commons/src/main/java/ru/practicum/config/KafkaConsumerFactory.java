package ru.practicum.config;

import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.consumer.enabled", havingValue = "true", matchIfMissing = false)
//как ключ, в одних приложениях создается в других нет
public class KafkaConsumerFactory {
    KafkaConsumer<String, SpecificRecordBase> consumer;
    final KafkaConsumerProperties config;

    @Bean
    public KafkaConsumer<String, SpecificRecordBase> consumer() {
        Properties properties = config.buildProperties();
        if (properties == null) {
            log.info("Настройки для consumer не загружены");
        }
        log.info("Загруженная конфигурация для consumer {}: ", properties);
        consumer = new KafkaConsumer<>(properties);
        log.info("Создан kafka-consumer {}", consumer);
        return consumer;
    }

    @PreDestroy
    public void closeConsumer() {
        if (consumer != null) {
            consumer.close();
            log.info("Kafka consumer закрыт");
        }
    }
}
