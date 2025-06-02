package ru.practicum.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.user.dto.UserShortDto;

@FeignClient(name = "user-service", path = "/admin/users", fallbackFactory = UserServiceFallback.class)
public interface UserServiceClient {

    @GetMapping("/{userId}")
    ResponseEntity<UserShortDto> getUserById(@PathVariable Long userId);
}
