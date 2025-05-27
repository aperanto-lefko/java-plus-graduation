package ru.practicum.user.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.StatClient;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UsersDtoGetParam;
import ru.practicum.user.service.UserService;

import java.util.List;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/admin/users")
public class UserController {
    private static final String APP_NAME = "ewm-main-service";

    private final UserService userService;
    private final StatClient statClient;

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(@ModelAttribute @Valid UsersDtoGetParam usersDtoGetParam) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getAll(usersDtoGetParam));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserDto> createUser(@RequestBody @Valid UserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.create(userDto));
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        userService.delete(userId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("Пользователь c id: " + userId + " удален");
    }
}