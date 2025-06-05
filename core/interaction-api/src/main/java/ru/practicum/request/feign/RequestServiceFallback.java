package ru.practicum.request.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.exception.ServiceUnavailableException;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class RequestServiceFallback {
    public Map<Long, Integer> getConfirmedRequestsCounts(
            @PathVariable("userId") Long userId,
            @RequestParam("eventIds") List<Long> eventIds) {
        log.warn("Активирован резервный вариант для getConfirmedRequestsCounts для запроса  с userId {} и eventIв {}", userId, eventIds);
        throw new ServiceUnavailableException("RequestService недоступен");
    }
}
