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
                                           HttpServletRequest rqt) {
        return eventService.getPublicEventById(id, rqt);
    }
}
