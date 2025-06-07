package ru.practicum.request.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.exception.ServiceUnavailableException;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class RequestServiceFallback {

    public Map<Long, Integer> getConfirmedRequest(@RequestParam List<Long> eventIds) {
        log.warn("Активирован резервный вариант для getConfirmedRequest для запроса  c eventIв {}", eventIds);
        throw new ServiceUnavailableException("RequestService недоступен");
    }
}
