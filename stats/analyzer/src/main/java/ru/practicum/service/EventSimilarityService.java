package ru.practicum.service;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public interface EventSimilarityService {
    void process (EventSimilarityAvro eventSimilarityAvro);
}
