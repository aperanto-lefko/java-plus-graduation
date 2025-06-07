package ru.practicum.user.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.exception.ServiceUnavailableException;
import ru.practicum.user.dto.UserShortDto;

import java.util.List;

@Component
@Slf4j
public class UserServiceFallback {

    public ResponseEntity<UserShortDto> getUserById(@PathVariable Long userId) {
        log.warn("Активирован резервный вариант для getUserById для пользователя с id {} ", userId);
        throw new ServiceUnavailableException("UserService недоступен");
    }

    public ResponseEntity<List<UserShortDto>> getUsersByIds(@RequestBody List<Long> userIds) {
        log.warn("Активирован резервный вариант для getUsersByIds для пользователей с ids {} ", userIds);
        throw new ServiceUnavailableException("UserService недоступен");
    }
}
