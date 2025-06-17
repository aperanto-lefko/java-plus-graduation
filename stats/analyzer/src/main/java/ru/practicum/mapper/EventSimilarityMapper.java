package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.model.EventSimilarity;


@Mapper(componentModel = "spring")
public interface EventSimilarityMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventIdA", source = "eventA")
    @Mapping(target = "eventIdB", source = "eventB")
    @Mapping(target = "score", source = "score")
    @Mapping(target = "timestamp", source = "timestamp")
    EventSimilarity toEntity(EventSimilarityAvro avro);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventIdA", ignore = true)
    @Mapping(target = "eventIdB", ignore = true)
    void updateEntityFromAvro(EventSimilarityAvro avro, @MappingTarget EventSimilarity entity);
}
