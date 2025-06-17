package ru.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.mapper.UserActionMapper;
import ru.practicum.model.UserAction;
import ru.practicum.repository.UserActionRepository;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UserActionServiceImpl implements UserActionService {
    final UserActionRepository userActionRepository;
    final UserActionMapper mapper;

    @Override
    public void process(UserActionAvro userActionAvro) {
        saveUserAction(userActionAvro);
    }

    private void saveUserAction(UserActionAvro userActionAvro) {
        log.info("Сохранение userActionAvro {} ", userActionAvro);
        UserAction userAction = userActionRepository.save(mapper.toEntity(userActionAvro));
        log.info("userActionAvro {} сохранена в базу данных", userActionAvro);
    }
}
