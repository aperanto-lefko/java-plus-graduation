package ru.practicum.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.user.dto.UserShortDto;

import java.util.List;

@FeignClient(name = "user-service", path = "/admin/users", fallbackFactory = UserServiceFallback.class)
public interface UserServiceClient {

    @GetMapping("/{userId}")
    ResponseEntity<UserShortDto> getUserById(@PathVariable Long userId);

    @PostMapping("/batch")
    ResponseEntity<List<UserShortDto>> getUsersByIds(@RequestBody List<Long> userIds);
}
