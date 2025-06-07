package ru.practicum.event.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.event.dto.EventFullDto;


@FeignClient(name = "event-service", path = "/admin/events", fallbackFactory = EventServiceFallback.class)
public interface EventServiceClient {
    @GetMapping("/{id}")
    EventFullDto getEventById(@PathVariable Long id);

    @GetMapping("/{eventId}/initiator/{userId}")
    EventFullDto getEventByIdAndInitiator(@PathVariable Long eventId,
                                          @PathVariable Long userId);
}
