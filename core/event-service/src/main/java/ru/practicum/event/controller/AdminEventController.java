package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventDtoGetParam;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Slf4j
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEvents(@ModelAttribute @Valid EventDtoGetParam prm) {
        return eventService.getEventsForAdmin(prm);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId,
                                    @RequestBody @Valid UpdateEventAdminRequest rq) {
        return eventService.updateEventByAdmin(eventId, rq);
    }
    @GetMapping("/{id}")
    public EventFullDto getPublicEventById(@PathVariable Long id,
                                           HttpServletRequest rqt) {
        return eventService.getEventById(id, rqt);
    }

    @GetMapping("/{eventId}/initiator/{userId}")
    public EventFullDto getEventByIdAndInitiator(@PathVariable Long eventId,
                                                 @PathVariable Long userId) {
        return eventService.getByIdAndInitiator(eventId, userId);
    }
}
