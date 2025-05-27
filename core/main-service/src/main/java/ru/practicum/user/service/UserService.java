package ru.practicum.user.service;

import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UsersDtoGetParam;
import ru.practicum.user.model.User;

import java.util.List;

public interface UserService {

    List<UserDto> getAll(UsersDtoGetParam usersDtoGetParam);

    UserDto create(UserDto userDto);

    void delete(Long id);

    User getUserById(Long id);
}