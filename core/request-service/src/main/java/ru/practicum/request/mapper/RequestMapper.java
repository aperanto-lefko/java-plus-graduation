package ru.practicum.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RequestMapper {
    @Mapping(source = "userId", target = "requester")
    @Mapping(source = "eventId", target = "event")
    ParticipationRequestDto toDto(Request request);

    List<ParticipationRequestDto> toDtoList(Iterable<Request> foundRequests);
}