package ru.practicum.request.service;

import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

public interface RequestService {
    ParticipationRequestDto createParticipationRequest(long userId, long eventId);

    List<ParticipationRequestDto> getAllByParticipantId(long userId);

    List<ParticipationRequestDto> getAllByInitiatorIdAndEventId(long userId, long eventId);

    EventRequestStatusUpdateResult changeEventRequestsStatusByInitiator(EventRequestStatusUpdateRequest updateRequest, long userId, long eventId);

    ParticipationRequestDto cancelParticipantRequest(long userId, long requestId);

    Map<Long, Integer> getConfirmedRequestsCounts(long serId, List<Long> eventIds);
}