package ru.practicum.properties;

import kafka.deserializer.EventSimilarityDeserializer;
import kafka.deserializer.UserActionDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Configuration
public class AnalyzerConfig {
    @Value("${kafka.consumer.bootstrap_server}")
    private String bootstrapServer;
    
    @Bean
    public KafkaConsumer<String, EventSimilarityAvro> eventSimilarityConsumer() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "analyzer_eventSimilarity_group");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "analyzer_consumer");
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventSimilarityDeserializer.class);
        return new KafkaConsumer<>(properties);
    }

    @Bean
    public KafkaConsumer<String, UserActionAvro> userActionConsumer() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "analyzer_userAction_group");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "analyzer_consumer");
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionDeserializer.class);
        return new KafkaConsumer<>(properties);
    }
}
