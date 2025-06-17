package ru.practicum.receiver;


import lombok.AccessLevel;

import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import ru.practicum.service.EventSimilarityService;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventSimilarityReceiver extends BaseKafkaReceiver<EventSimilarityAvro> {
    @Value("${kafka.topics.events-similarity}")
    String inputTopic;
    final EventSimilarityService eventSimilarityService;

    public EventSimilarityReceiver(KafkaConsumer<String, EventSimilarityAvro> consumer,
                                   EventSimilarityService eventSimilarityService) {
        super(consumer);
        this.eventSimilarityService=eventSimilarityService;
    }

    @Override
    protected String getInputTopic() {
        return inputTopic;
    }

    @Override
    protected void processMessage(EventSimilarityAvro message) {
        eventSimilarityService.process(message);
    }
}

