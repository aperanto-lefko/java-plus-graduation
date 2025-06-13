package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventDtoGetParam;

import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getPublicEvents(@ModelAttribute EventDtoGetParam prm,
                                               HttpServletRequest rqt) {
        return eventService.getPublicEvents(prm, rqt);
    }

    @GetMapping("/{id}")
    public EventFullDto getPublicEventById(@PathVariable Long id,
                                           @RequestHeader(value = "X-EWM-USER-ID", required = false) Long userId,
                                           HttpServletRequest rqt) {
        return eventService.getPublicEventById(id, userId, rqt);
    }

    @PutMapping("/{eventId}/like")
    public void likeEvent(@RequestHeader("X-EWM-USER-ID") Long userId, @PathVariable Long eventId) {
        eventService.likeEvent(eventId, userId);
    }

}
