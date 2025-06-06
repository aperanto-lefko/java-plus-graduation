package ru.practicum.request.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;


import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service", path = "", fallbackFactory = RequestServiceFallback.class )
public interface RequestServiceClient {
//    @GetMapping("/requests/count-confirmed")
//    Map<Long, Integer> getConfirmedRequestsCounts(
//            @PathVariable("userId") Long userId,
//            @RequestParam("eventIds") List<Long> eventIds);

    @GetMapping("/requests/count-confirmed")
    Map<Long, Integer> getConfirmedRequest(@RequestParam List<Long> eventIds);

}
