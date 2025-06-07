package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.View;
import ru.practicum.event.repository.ViewRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ViewServiceImpl implements ViewService {
    private final ViewRepository viewRepository;

    @Override
    public void saveViews(List<Event> events, HttpServletRequest rqt) {
        log.info("Сохранение просмотров для ip {} и списка события", rqt.getRemoteAddr());
        String ip = rqt.getRemoteAddr();
        log.info("Проверка списка просмотров на наличие в базе");
        List<View> existingViews = viewRepository.findByEventInAndIp(events, rqt.getRemoteAddr());

        Set<Long> existingEventIds = existingViews.stream() //получаем список ids существующих записей
                .map(view -> view.getEvent().getId())
                .collect(Collectors.toSet());

        List<View> viewsToSave = events.stream()
                .filter(event -> !existingEventIds.contains(event.getId()))
                .map(event -> View.builder()
                        .event(event)
                        .ip(ip)
                        .viewTime(LocalDateTime.now())
                        .build())
                .toList();

        // Сохранение только новых записей
        if (!viewsToSave.isEmpty()) {
            log.info("Сохранение новых просмотров в базу");
            viewRepository.saveAll(viewsToSave);
        }
    }

    @Override
    public void saveView(Event ev, HttpServletRequest rqt) {
        log.info("Сохранение просмотра для ip {} и события {}", rqt.getRemoteAddr(), ev);
        if (!viewRepository.existsByEventIdAndIp(ev.getId(), rqt.getRemoteAddr())) {
            viewRepository.save(View.builder()
                    .event(ev)
                    .ip(rqt.getRemoteAddr())
                    .viewTime(LocalDateTime.now())
                    .build());
        }
    }
}
