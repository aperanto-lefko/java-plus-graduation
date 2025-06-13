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
@Getter
@Setter
@Slf4j
public class KafkaProducerProperties {
    String bootstrapServer;
    String keySerializeClass;
    String valueSerializeClass;

    public Properties buildProperties() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializeClass);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializeClass);
        return properties;
    }

    @PostConstruct
    public void init() {
        log.info("Loaded Kafka producer config: bootstrap={}, keySerializer={}, valueSerializer={}",
                bootstrapServer, keySerializeClass, valueSerializeClass);
    }
}
