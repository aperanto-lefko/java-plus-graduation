package ru.practicum.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping //("/users/{userId}")
public class PrivateRequestController {
    private final RequestService requestService;

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(@PathVariable(name = "userId") @Positive long userId,
                                          @RequestParam(name = "eventId") @Positive long eventId) {
        var response = requestService.createParticipationRequest(userId, eventId);
        return response;
    }

    @GetMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getAllByParticipantId(@PathVariable(name = "userId") @Positive long userId) {
        var response = requestService.getAllByParticipantId(userId);
        return response;
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelParticipantRequest(@PathVariable(name = "userId") @Positive long userId,
                                                            @PathVariable(name = "requestId") @Positive long requestId) {
        var response = requestService.cancelParticipantRequest(userId, requestId);
        return response;
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getAllEventsOfInitiator(@PathVariable(name = "userId") @Positive long userId,
                                                                 @PathVariable(name = "eventId") @Positive long eventId) {
        var response = requestService.getAllByInitiatorIdAndEventId(userId, eventId);
        return response;
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult changeEventRequestsStatus(@PathVariable(name = "userId") @Positive long userId,
                                                                    @PathVariable(name = "eventId") @Positive long eventId,
                                                                    @RequestBody @Valid EventRequestStatusUpdateRequest updateRequest) {
        var response = requestService.changeEventRequestsStatusByInitiator(updateRequest, userId, eventId);
        return response;
    }

    //добавлено
//    @GetMapping("/requests/count-confirmed")
//    @ResponseStatus(HttpStatus.OK)
//    public Map<Long, Integer> getConfirmedRequest(@PathVariable(name = "userId") @Positive long userId,
//                                                  @RequestParam List<Long> eventIds) {
//        return requestService.getConfirmedRequestsCounts(userId, eventIds);
//    }
    @GetMapping("/requests/count-confirmed")
    @ResponseStatus(HttpStatus.OK)
    public Map<Long, Integer> getConfirmedRequest(@RequestParam List<Long> eventIds) {
        return requestService.getConfirmedRequestsCounts(eventIds);
    }


}