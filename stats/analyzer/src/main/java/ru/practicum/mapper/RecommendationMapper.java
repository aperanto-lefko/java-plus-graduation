package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.RecommendedEvent;
import ru.practicum.evm.stats.proto.RecommendedEventProto;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {
    RecommendedEventProto toProto(RecommendedEvent event);
}
