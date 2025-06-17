package ru.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.mapper.EventSimilarityMapper;
import ru.practicum.model.EventSimilarity;
import ru.practicum.repository.EventSimilarityRepository;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class EventSimilarityServiceImpl implements EventSimilarityService {
    final EventSimilarityRepository eventSimilarityRepository;
    final EventSimilarityMapper mapper;


    @Override
    public void process(EventSimilarityAvro eventSimilarityAvro) {
        Objects.requireNonNull(eventSimilarityAvro, "EventSimilarityAvro не может быть null");

        eventSimilarityRepository
                .findByEventIdAAndEventIdB(eventSimilarityAvro.getEventA(), eventSimilarityAvro.getEventB())
                .ifPresentOrElse(
                        existing -> updateSimilarity(eventSimilarityAvro, existing),
                        () -> createSimilarity(eventSimilarityAvro)
                );
    }

    private void createSimilarity(EventSimilarityAvro avro) {
        log.info("Создание нового similarity из events {} and {}", avro.getEventA(), avro.getEventB());
        EventSimilarity entity = mapper.toEntity(avro);
        eventSimilarityRepository.save(entity);
        log.info("Создан новый similarity с ID: {}", entity.getId());
    }

    private void updateSimilarity(EventSimilarityAvro avro, EventSimilarity existing) {
        log.info("Обновление similarity для событий A {} и B {}", avro.getEventA(), avro.getEventB());
        mapper.updateEntityFromAvro(avro, existing);
        eventSimilarityRepository.save(existing);
        log.info("Обновление similarity score до {}", existing.getScore());
    }

}
