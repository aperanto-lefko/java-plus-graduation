package ru.practicum.event.service;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.service.CategoryService;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;

import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.QEvent;
import ru.practicum.event.model.State;
import ru.practicum.event.model.StateAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictStateException;
import ru.practicum.exception.ConflictTimeException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.feign.UserServiceClient;


import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final LocationService locationService;
    private final CategoryService categoryService;
    private final ViewService viewService;
    private final EventMapper mp;
    private final LocationMapper lmp;
    private final QEvent event = QEvent.event;
    private final UserServiceClient userServiceClient;

    @Transactional
    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        Event ev = mp.toEntity(newEventDto);
        ev.setCategory(categoryService.getCategory(ev.getCategory().getId()));
        ev.setUserId(getUserById(userId).getId());
        log.info("Создание события {}", ev);
        return mp.toEventFullDto(eventRepository.save(ev));
    }

    @Override
    public List<EventShortDto> getEventsForUser(EventDtoGetParam prm) {
        log.info("Получение списка мероприятий для пользователя с id {} ", prm.getUserId());
        Predicate predicate = event.userId.eq(prm.getUserId());
        PageRequest pageRequest = PageRequest.of(prm.getFrom(), prm.getSize());
        List<Event> events = eventRepository.findAll(predicate, pageRequest).getContent();
        return mp.toEventShortDto(events);
    }

    @Override
    public EventFullDto getEventByIdForUser(EventDtoGetParam prm) {
        Predicate predicate = event.userId.eq(prm.getUserId())
                .and(event.id.eq(prm.getEventId()));
        Event ev = eventRepository.findOne(predicate)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с id %d для пользователя с id %d не найдено.",
                                prm.getEventId(), prm.getUserId())));
        log.info("Получение события с id {}  для пользователя с id {}", prm.getEventId(), prm.getUserId());
        return mp.toEventFullDto(ev);
    }

    @Override
    public List<EventFullDto> getEventsForAdmin(EventDtoGetParam prm) {
        Predicate predicate = null;
        if (prm.getUsers() != null && !prm.getUsers().isEmpty()) {
            predicate = ExpressionUtils.and(predicate, event.userId.in(prm.getUsers()));
        }
        if (prm.getStates() != null && !prm.getStates().isEmpty()) {
            List<State> states = prm.getStates().stream()
                    .map(State::valueOf) // Преобразуем строки в перечисление
                    .toList();
            predicate = ExpressionUtils.and(predicate, event.state.in(states));
        }
        if (prm.getCategories() != null && !prm.getCategories().isEmpty()) {
            predicate = ExpressionUtils.and(predicate, event.category.id.in(prm.getCategories()));
        }
        if (prm.getRangeStart() != null && prm.getRangeEnd() != null) {
            dateValid(prm.getRangeStart(), prm.getRangeEnd());
            predicate = ExpressionUtils.and(predicate, event.eventDate.between(prm.getRangeStart(), prm.getRangeEnd()));
        }
        PageRequest pageRequest = PageRequest.of(prm.getFrom(), prm.getSize());
        List<Event> events;
        events = (predicate == null)
                ? eventRepository.findAll(pageRequest).getContent()
                : eventRepository.findAll(predicate, pageRequest).getContent();
        log.info("Получение списка событий администратором с параметрами {} и предикатом {}", prm, predicate);
        return mp.toEventFullDto(events);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long id, UpdateEventAdminRequest rq) {
        Event ev = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с id %d не найдено.", id)));
        if ((rq.getStateAction() == StateAction.PUBLISH_EVENT && ev.getState() != State.PENDING) ||
                (rq.getStateAction() == StateAction.REJECT_EVENT && ev.getState() == State.PUBLISHED)) {
            throw new ConflictStateException(
                    (rq.getStateAction() == StateAction.PUBLISH_EVENT) ?
                            "Невозможно опубликовать событие, так как текущий статус не PENDING"
                            : "Нельзя отменить публикацию, так как событие уже опубликовано");
        }
        ev.setState(State.CANCELED);
        if (rq.getLocation() != null) {
            Location sLk = locationService.getLocation(lmp.toLocation(rq.getLocation()));
            ev.setLocation(sLk);
        }
        mp.updateFromAdmin(rq, ev);
        ev.setState(rq.getStateAction() == StateAction.PUBLISH_EVENT ? State.PUBLISHED : State.CANCELED);
        log.info("Обновление события с id {} администратором с параметрами {}", id, rq);
        return mp.toEventFullDto(eventRepository.save(ev));
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest rq) {
        if (rq.getEventDate() != null && rq.getEventDate().isBefore(LocalDateTime.now().plusHours(2L))) {
            throw new ConflictTimeException("Время не может быть раньше, через два часа от текущего момента");
        }
        Event ev = eventRepository.findByIdAndUserId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с id %d для пользователя с id %d не найдено.", eventId, userId)));
        if (ev.getState() == State.PUBLISHED) {
            throw new ConflictStateException("Изменить можно только неопубликованное событие");
        }
        if (rq.getStateAction() != null) {
            switch (rq.getStateAction()) {
                case SEND_TO_REVIEW -> ev.setState(State.PENDING);
                case CANCEL_REVIEW -> ev.setState(State.CANCELED);
                default -> throw new IllegalArgumentException("Неизвестный статус: " + rq.getStateAction());
            }
        }
        if (rq.getLocation() != null) {
            ev.setLocation(locationService.getLocation(lmp.toLocation(rq.getLocation())));
        }
        mp.updateFromUser(rq, ev);
        return mp.toEventFullDto(eventRepository.save(ev));
    }

    @Override
    @Transactional
    public List<EventShortDto> getPublicEvents(EventDtoGetParam prm, HttpServletRequest rqt) {
        Predicate predicate = event.state.eq(State.PUBLISHED);
        if (prm.getText() != null && !prm.getText().isEmpty()) {
            predicate = ExpressionUtils.and(predicate, (event.annotation.containsIgnoreCase(prm.getText())).or(
                    event.description.containsIgnoreCase(prm.getText())));
        }
        if (prm.getCategories() != null && !prm.getCategories().isEmpty()) {
            predicate = ExpressionUtils.and(predicate, event.category.id.in(prm.getCategories()));
        }
        if (prm.getPaid() != null) {
            predicate = ExpressionUtils.and(predicate, event.paid.eq(prm.getPaid()));
        }
        if (prm.getRangeStart() != null && prm.getRangeEnd() != null) {
            dateValid(prm.getRangeStart(), prm.getRangeEnd());
            predicate = ExpressionUtils.and(predicate, event.eventDate.between(prm.getRangeStart(), prm.getRangeEnd()));
        } else {
            predicate = ExpressionUtils.and(predicate, event.eventDate.gt(LocalDateTime.now())); //TODO: проверить
        }
        if (prm.getOnlyAvailable() != null && prm.getOnlyAvailable()) { //проверка есть ли еще места на мероприятие
            predicate = ExpressionUtils.and(predicate, (event.participantLimit.eq(0)).or(
                    event.participantLimit.subtract(event.confirmedRequests).gt(0)));
        }
        Sort sort = Sort.unsorted();
        if (prm.getSort() != null) {
            if (prm.getSort().equals("EVENT_DATE")) {
                sort = Sort.by(Sort.Direction.ASC, "eventDate");
            } else if (prm.getSort().equals("VIEWS")) {
                sort = Sort.by(Sort.Direction.DESC, "views");
            }
        }
        PageRequest pageRequest = PageRequest.of(prm.getFrom(), prm.getSize(), sort);
        List<Event> events = eventRepository.findAll(predicate, pageRequest).getContent();
        if (!events.isEmpty()) {
            viewService.saveViews(events, rqt);
        }
        return mp.toEventShortDto(events);
    }

    @Override
    @Transactional
    public EventFullDto getPublicEventById(Long id, HttpServletRequest rqt) {
        Predicate predicate = event.state.eq(State.PUBLISHED).and(event.id.eq(id));
        Event ev = eventRepository.findOne(predicate)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с id %d не найдено.", id)));
        viewService.saveView(ev, rqt);
        return mp.toEventFullDto(ev);
    }

    @Override
    public List<Event> getAllEventByIds(List<Long> ids) {
        return eventRepository.findAllById(ids);
    }

    @Override
    public Event getPublicEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Не найдено события с id: " + id));
    }

    private void dateValid(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new ValidationException("Дата начала события позже даты окончания");
        }
    }
    private UserShortDto getUserById(Long userId) {
        UserShortDto user = userServiceClient.getUserById(userId).getBody();
        if (user == null) {
            throw new NotFoundException("Пользователь не найден с id: " + userId);
        }
        return user;
    }
}

