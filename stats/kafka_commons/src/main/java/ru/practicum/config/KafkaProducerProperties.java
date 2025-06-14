package ru.practicum.config;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "kafka.producer")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Getter
@Setter
public class KafkaProducerProperties {
    String bootstrapServer;
    String keySerializeClass;
    String valueSerializeClass;

    Integer lingerMs;
    Integer batchSize;
    Integer maxInFlightRequests;

    public Properties buildProperties() {

        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializeClass);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializeClass);

        properties.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        properties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, maxInFlightRequests);
        return properties;
    }

    @PostConstruct
    public void init() {
        log.info("Loaded Kafka producer config: bootstrap={}, keySerializer={}, valueSerializer={}, lingerMs={}, batchSize={}, maxInFlight={}",
                bootstrapServer, keySerializeClass, valueSerializeClass, lingerMs, batchSize, maxInFlightRequests);
    }
}
