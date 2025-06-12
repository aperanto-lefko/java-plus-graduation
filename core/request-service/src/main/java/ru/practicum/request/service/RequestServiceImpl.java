package ru.practicum.request.service;

import com.google.protobuf.Timestamp;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.client.CollectorClient;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.State;
import ru.practicum.event.feign.EventServiceClient;

import ru.practicum.evm.stats.proto.ActionTypeProto;
import ru.practicum.evm.stats.proto.UserActionProto;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.dto.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.feign.UserServiceClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    EventServiceClient eventServiceClient;
    RequestRepository requestRepository;
    RequestMapper requestMapper;
    UserServiceClient userServiceClient;
    CollectorClient collectorClient;

    @Override
    public ParticipationRequestDto createParticipationRequest(long userId, long eventId) {
        log.info("Создание запроса на участии в мероприятии для userId {} с eventId {}", userId, eventId);
        EventFullDto event = eventServiceClient.getEventById(eventId);

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConditionsNotMetException("Нельзя участвовать в неопубликованном событии");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Инициатор события не может добавить запрос на участие в своём событии");
        }

        checkParticipantLimit(event.getParticipantLimit(), getConfirmedRequests(eventId));
        UserShortDto user = getUserById(userId);

        RequestStatus status = RequestStatus.PENDING;
        if (event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
        } else if (!event.isRequestModeration()) {
            status = RequestStatus.CONFIRMED;
        }

        Request request = Request.builder()
                .eventId(event.getId())
                .userId(userId)
                .status(status)
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))
                .build();

        try {
            request = requestRepository.save(request);
        } catch (DataIntegrityViolationException e) {
            throw new ConditionsNotMetException("Нельзя добавить повторный запрос на участие в событии");
        }

        log.info("Отправка регистрации на мероприятие в collector");
        collectorClient.sendUserAction(userId, eventId, ActionTypeProto.ACTION_REGISTER);
        return requestMapper.toDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getAllByParticipantId(long userId) {
        List<Request> foundRequests = requestRepository.findAllByUserId(userId);
        return requestMapper.toDtoList(foundRequests);
    }

    @Override
    public List<ParticipationRequestDto> getAllByInitiatorIdAndEventId(long userId, long eventId) {
        EventFullDto event;
        try {
            event = eventServiceClient.getEventByIdAndInitiator(eventId, userId);
        } catch (FeignException.NotFound ex) {
            throw new NotFoundException(
                    String.format("Событие с id %d для пользователя с id %d не найдено", eventId, userId)
            );
        }
        List<Request> foundRequests = requestRepository.findAllByEventId(event.getId());
        return requestMapper.toDtoList(foundRequests);
    }

    @Override
    public EventRequestStatusUpdateResult changeEventRequestsStatusByInitiator(EventRequestStatusUpdateRequest updateRequest, long userId, long eventId) {
        EventFullDto event = eventServiceClient.getEventById(eventId);

        List<Long> requestIds = updateRequest.getRequestIds();
        List<Request> foundRequests = requestRepository.findAllById(requestIds);
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (Request request : foundRequests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConditionsNotMetException("Заявка должна находиться в ожидании");
            }
        }

        switch (updateRequest.getStatus()) {
            case CONFIRMED -> handleConfirmedRequests(event, foundRequests, result, confirmed, rejected);
            case REJECTED -> handleRejectedRequests(foundRequests, result, rejected);
        }

        result.setConfirmedRequests(confirmed);
        result.setRejectedRequests(rejected);

        return result;
    }

    @Override
    public ParticipationRequestDto cancelParticipantRequest(long userId, long requestId) {
        Request request = requestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException(String.format("Запрос на участие в событии с id запроса=%d не найден", requestId))
        );

        Long requesterId = request.getUserId();
        if (!requesterId.equals(userId)) {
            throw new ConditionsNotMetException("Пользователь не является участником в запросе на участие в событии");
        }

        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);

        return requestMapper.toDto(request);
    }

    private void checkParticipantLimit(int participantLimit, int confirmedRequests) {
        if (confirmedRequests >= participantLimit && participantLimit != 0) {
            throw new ConditionsNotMetException("У события заполнен лимит участников");
        }
    }

    private int getConfirmedRequests(long eventId) {
        return requestRepository.findCountOfConfirmedRequestsByEventId(eventId);
    }

    private void updateStatus(RequestStatus status, List<Long> ids) {
        requestRepository.updateStatus(status, ids);
    }

    private void handleConfirmedRequests(EventFullDto event, List<Request> foundRequests, EventRequestStatusUpdateResult result, List<ParticipationRequestDto> confirmed, List<ParticipationRequestDto> rejected) {
        int confirmedRequests = getConfirmedRequests(event.getId());
        int participantLimit = event.getParticipantLimit();
        if (participantLimit == 0 || !event.isRequestModeration()) {
            result.setConfirmedRequests(requestMapper.toDtoList(foundRequests));
            return;
        }
        checkParticipantLimit(participantLimit, confirmedRequests);
        for (Request request : foundRequests) {
            if (confirmedRequests >= participantLimit) {
                rejected.add(requestMapper.toDto(request));
                continue;
            }
            request.setStatus(RequestStatus.CONFIRMED);
            confirmed.add(requestMapper.toDto(request));
            ++confirmedRequests;
        }
        List<Long> confirmedRequestIds = confirmed.stream().map(ParticipationRequestDto::getId).toList();
        updateStatus(RequestStatus.CONFIRMED, confirmedRequestIds);
    }

    @Override
    public Map<Long, Integer> getConfirmedRequestsCounts(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Object[]> counts = requestRepository.countConfirmedRequestsByEventIds(eventIds);
        return counts.stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],  // eventId
                        arr -> ((Number) arr[1]).intValue()  // count
                ));
    }

    private void handleRejectedRequests(List<Request> foundRequests, EventRequestStatusUpdateResult result, List<ParticipationRequestDto> rejected) {
        for (Request request : foundRequests) {
            request.setStatus(RequestStatus.REJECTED);
            rejected.add(requestMapper.toDto(request));
        }
        List<Long> rejectedRequestIds = rejected.stream().map(ParticipationRequestDto::getId).toList();
        updateStatus(RequestStatus.REJECTED, rejectedRequestIds);
    }

    private UserShortDto getUserById(Long userId) {
        UserShortDto user = userServiceClient.getUserById(userId).getBody();
        if (user == null) {
            throw new NotFoundException("Пользователь не найден с id: " + userId);
        }
        return user;
    }
}