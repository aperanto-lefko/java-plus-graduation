package ru.practicum.event.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.event.dto.EventFullDto;


@FeignClient(name = "main-service", path = "/events", fallbackFactory = EventServiceFallback.class)
public interface EventServiceClient {
    @GetMapping("/{id}")
    EventFullDto getPublicEventById(@PathVariable Long id);
}
