package ru.practicum.user.service;

import com.querydsl.core.types.Predicate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UsersDtoGetParam;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.QUser;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * Возвращает список пользователей с учетом параметров фильтрации и пагинации.
     *
     * @param usersDtoGetParam объект с параметрами фильтрации и пагинации
     * @return список DTO пользователей
     */
    @Override
    public List<UserDto> getAll(UsersDtoGetParam usersDtoGetParam) {
        log.info("Получение списка пользователей с параметрами: from={}, size={}, ids={}",
                usersDtoGetParam.getFrom(), usersDtoGetParam.getSize(), usersDtoGetParam.getIds());

        QUser user = QUser.user;
        Predicate predicate = userPredicate(usersDtoGetParam, user);
        PageRequest pageRequest = PageRequest.of(usersDtoGetParam.getFrom(), usersDtoGetParam.getSize());

        List<User> users;

        // Если предикат равен null, возвращаем всех пользователей с пагинацией, иначе по предикату с пагинацией
        if (predicate == null) {
            log.info("Запрос всех пользователей с пагинацией: from={}, size={}", usersDtoGetParam.getFrom(),
                    usersDtoGetParam.getSize());
            users = userRepository.findAll(pageRequest).getContent();
        } else {
            log.info("Запрос пользователей по предикату с пагинацией: from={}, size={}, predicate={}",
                    usersDtoGetParam.getFrom(), usersDtoGetParam.getSize(), predicate);
            users = userRepository.findAll(predicate, pageRequest).getContent();
        }

        return users.stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    /**
     * Создает нового пользователя.
     *
     * @param userDto DTO пользователя
     * @return созданный DTO пользователя
     */
    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        log.info("Создание нового пользователя с email: {}", userDto.getEmail());
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userDto)));
    }

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     */
    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Не найден пользователь с id: " + id);
        }
        log.info("Удаление пользователя с id: {}", id);
        userRepository.deleteById(id);
    }

    /**
     * Формирует предикат для фильтрации пользователей по идентификаторам.
     *
     * @param usersDtoGetParam объект с параметрами фильтрации
     * @param user             Q-класс пользователя
     * @return предикат для фильтрации или null, если список идентификаторов пуст
     */
    private Predicate userPredicate(UsersDtoGetParam usersDtoGetParam, QUser user) {
        if (usersDtoGetParam.getIds() == null || usersDtoGetParam.getIds().isEmpty()) {
            return null;
        }

        return user.id.in(usersDtoGetParam.getIds());
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }
}