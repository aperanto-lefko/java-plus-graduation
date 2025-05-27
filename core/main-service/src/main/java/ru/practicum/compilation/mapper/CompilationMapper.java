package ru.practicum.compilation.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.practicum.compilation.dto.AdminCompilationDto;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.mapper.EventMapper;

@Mapper(uses = {EventMapper.class})
public interface CompilationMapper {

    CompilationMapper INSTANCE = Mappers.getMapper(CompilationMapper.class);

    @Mapping(target = "events", source = "events")
    @Mapping(target = "id", source = "id")
    CompilationDto toDto(Compilation compilation);

    @Mapping(target = "events", ignore = true)
    @Mapping(target = "id", ignore = true)
    Compilation toEntity(AdminCompilationDto adminCompilationDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Compilation updateCompilationFromDto(AdminCompilationDto adminCompilationDto, @MappingTarget Compilation compilation);
}