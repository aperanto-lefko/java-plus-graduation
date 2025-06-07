package ru.practicum.request.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service", path = "", fallbackFactory = RequestServiceFallback.class )
public interface RequestServiceClient {

    @GetMapping("/requests/count-confirmed")
    Map<Long, Integer> getConfirmedRequest(@RequestParam List<Long> eventIds);

}
