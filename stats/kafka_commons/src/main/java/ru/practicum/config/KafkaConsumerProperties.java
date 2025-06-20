package ru.practicum.config;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "kafka.consumer")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Getter
@Setter
public class KafkaConsumerProperties {
    String bootstrapServer;
    String clientId;
    String groupId;
    String autoOffsetReset;
    String keyDeserializerClass;
    String valueDeserializerClass;

    public Properties buildProperties() {

        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset); // Что делать при отсутствии оффсета: "earliest", "latest", "none"
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializerClass);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializerClass);
        return properties;
    }

    @PostConstruct
    public void init() {
        log.info("""
        Loaded Kafka consumer configuration:
        bootstrapServer={}
        clientId={}
        groupId={}
        autoOffsetReset={}
        keyDeserializerClass={}
        valueDeserializerClass={}
        """,
                bootstrapServer,
                clientId,
                groupId,
                autoOffsetReset,
                keyDeserializerClass,
                valueDeserializerClass);

    }
}
