package ru.practicum.event.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.exception.ServiceUnavailableException;

@Component
@Slf4j
public class EventServiceFallback {
    public EventFullDto getPublicEventById(@PathVariable Long id) {
        log.warn("Активирован резервный вариант для getPublicEventById для события с id {} ", id);
        throw new ServiceUnavailableException("EventService недоступен");
    }
}
