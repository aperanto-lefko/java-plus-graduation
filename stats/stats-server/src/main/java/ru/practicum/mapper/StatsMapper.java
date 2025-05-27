package ru.practicum.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.model.Stats;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StatsMapper {
    @Mapping(target = "hits", ignore = true)
    StatsDto toDto(Stats stats);

    @Mapping(target = "id", ignore = true)
    Stats toEntity(HitDto hitDto);
}
