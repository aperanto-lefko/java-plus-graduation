package ru.practicum.event.service;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.service.CategoryService;
import ru.practicum.client.AnalyzerClient;
import ru.practicum.client.CollectorClient;
import ru.practicum.event.dto.EventDtoGetParam;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;

import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.QEvent;
import ru.practicum.event.dto.State;
import ru.practicum.event.dto.StateAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.evm.stats.proto.ActionTypeProto;
import ru.practicum.evm.stats.proto.RecommendedEventProto;
import ru.practicum.exception.ConflictStateException;
import ru.practicum.exception.ConflictTimeException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.feign.RequestServiceClient;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.feign.UserServiceClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventServiceImpl implements EventService {
    EventRepository eventRepository;
    LocationService locationService;
    CategoryService categoryService;
    ViewService viewService;
    EventMapper mp;
    LocationMapper lmp;
    QEvent event = QEvent.event;
    UserServiceClient userServiceClient;
    RequestServiceClient requestServiceClient;
    CollectorClient collectorClient;
    AnalyzerClient analyzerClient;

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


        //добавление подтвержденных просмотров
        Map<Long, Integer> confirmedRequestsMap = getConfirmedRequestMap(events);
        List<EventShortDto> dtos = toEventShortDtoAddUserList(events);
        dtos.forEach(dto ->
                dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(dto.getId(), 0)));

        return dtos;
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

        //добавление подтвержденных просмотров
        int confirmedRequests = requestServiceClient.getConfirmedRequest(List.of(ev.getId()))
                .getOrDefault(ev.getId(), 0);

        EventFullDto dto = addUserShortDtoToFullDto(ev, prm.getUserId());
        dto.setConfirmedRequests(confirmedRequests);
        return dto;
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
        log.info("Получение списка событий администратором с параметрами {} и предикатом {}", prm, predicate);
        events = (predicate == null)
                ? eventRepository.findAll(pageRequest).getContent()
                : eventRepository.findAll(predicate, pageRequest).getContent();

        Map<Long, Integer> confirmedRequestsMap = getConfirmedRequestMap(events);
        List<EventFullDto> dtos = toEventFullDtoAddUserList(events);
        dtos.forEach(dto ->
                dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(dto.getId(), 0)));

        return dtos;
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
        Event savedEvent = eventRepository.save(ev);

        log.info("Запрос в requestService, добавление количества запросов");
        int confirmedRequests = requestServiceClient.getConfirmedRequest(List.of(savedEvent.getId()))
                .getOrDefault(ev.getId(), 0);
        EventFullDto eventFullDto = addUserShortDtoToFullDto(savedEvent, savedEvent.getUserId());
        eventFullDto.setConfirmedRequests(confirmedRequests);
        return eventFullDto;
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
        Event savedEvent = eventRepository.save(ev);
        int confirmedRequests = requestServiceClient.getConfirmedRequest(List.of(savedEvent.getId()))
                .getOrDefault(ev.getId(), 0);
        EventFullDto eventFullDto = addUserShortDtoToFullDto(savedEvent, savedEvent.getUserId());
        eventFullDto.setConfirmedRequests(confirmedRequests);
        return eventFullDto;
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

        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Integer> confirmedRequestsMap = requestServiceClient.getConfirmedRequest(eventIds);
        if (prm.getOnlyAvailable() != null && prm.getOnlyAvailable() && !events.isEmpty()) {
            events = events.stream()
                    .filter(event -> {
                        int confirmed = confirmedRequestsMap.getOrDefault(event.getId(), 0);
                        return event.getParticipantLimit() == 0 ||
                                event.getParticipantLimit() > confirmed;
                    })
                    .collect(Collectors.toList());
        }

//        if (!events.isEmpty()) {
//            viewService.saveViews(events, rqt);
//        }

        List<EventShortDto> dtos = toEventShortDtoAddUserList(events);
        dtos.forEach(dto ->
                dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(dto.getId(), 0)));

        return dtos;
    }

    @Override
    @Transactional
    public EventFullDto getPublicEventById(Long id, Long userId, HttpServletRequest rqt) {
        Predicate predicate = event.state.eq(State.PUBLISHED).and(event.id.eq(id));
        Event ev = eventRepository.findOne(predicate)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с id %d не найдено.", id)));
        viewService.saveView(ev, rqt);
        int confirmedRequests = requestServiceClient.getConfirmedRequest(List.of(ev.getId()))
                .getOrDefault(ev.getId(), 0);
        EventFullDto eventFullDto = addUserShortDtoToFullDto(ev, ev.getUserId());
        eventFullDto.setConfirmedRequests(confirmedRequests);

        log.info("Отправка регистрации на мероприятие в collector");
        collectorClient.sendUserAction(userId, id, ActionTypeProto.ACTION_VIEW);

        return eventFullDto;
    }

    // для request service
    @Override
    @Transactional
    public EventFullDto getEventById(Long id, HttpServletRequest rqt) {
        Event ev = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с id %d не найдено.", id)));
        return addUserShortDtoToFullDto(ev, ev.getUserId());
    }

    //для request service
    @Override
    public EventFullDto getByIdAndInitiator(Long eventId, Long userId) {
        Event ev = eventRepository.findByIdAndUserId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id %d и userId %d не найдено.", eventId, userId)));
        return addUserShortDtoToFullDto(ev, ev.getUserId());
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

    @Override
    public void likeEvent(Long eventId, Long userId) {
        if (requestServiceClient.isRegistered(eventId, userId)) {
            log.info("Отправка регистрации на мероприятие в collector");
            collectorClient.sendUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
        } else {
            throw new NotFoundException(String.format("Пользователь userId {} не зарегистрирован на событие eventId {}", userId, eventId));
        }
    }
    @Override
    public List<EventShortDto> getRecommendations(Long userId, int limit) {
        log.info("Поиск рекомендаций для пользователя userId {} с limit {}", userId, limit);
        List<RecommendedEventProto> recommendedEvents = analyzerClient
                .getRecommendationsForUser(userId, limit)
                .toList();
        if (recommendedEvents.isEmpty()) {
            log.info("Рекомендации не найдены для пользователя: {}", userId);
            return Collections.emptyList();
        }
        List<Long> recommendedEventIds = recommendedEvents.stream()
                .map(RecommendedEventProto::getEventId)
                .toList();

        List<Event> events = eventRepository.findAllByIdIn(recommendedEventIds);

        return events.stream()
                .map(mp::toEventShortDto)
                .toList();
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

    public List<EventFullDto> toEventFullDtoAddUserList(List<Event> events) {
        Map<Long, UserShortDto> userMap = getUserMap(events);
        return events.stream()
                .map(event -> {
                    EventFullDto dto = mp.toEventFullDto(event);
                    dto.setInitiator(userMap.get(event.getUserId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<EventShortDto> toEventShortDtoAddUserList(List<Event> events) {
        Map<Long, UserShortDto> userMap = getUserMap(events);
        return events.stream()
                .map(event -> {
                    EventShortDto dto = mp.toEventShortDto(event);
                    dto.setInitiator(userMap.get(event.getUserId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private Map<Long, UserShortDto> getUserMap(List<Event> events) {
        Set<Long> userIds = events.stream()
                .map(Event::getUserId)
                .collect(Collectors.toSet());

        List<UserShortDto> users = userServiceClient.getUsersByIds(new ArrayList<>(userIds)).getBody();
        if (users == null) {
            return Collections.emptyMap(); // Защита от NPE
        }

        return users.stream()
                .collect(Collectors.toMap(UserShortDto::getId, Function.identity()));
    }

    private EventFullDto addUserShortDtoToFullDto(Event event, Long userId) {
        EventFullDto dto = mp.toEventFullDto(event);
        UserShortDto userDto = userServiceClient.getUserById(userId).getBody();
        dto.setInitiator(userDto);
        return dto;
    }

    private Map<Long, Integer> getConfirmedRequestMap(List<Event> events) {
        log.info("Запрос в requestService, добавление количества запросов для {}", events);
        Map<Long, Integer> confirmedRequestsMap;
        if (!events.isEmpty()) {
            List<Long> eventIds = events.stream().map(Event::getId).toList();
            confirmedRequestsMap = requestServiceClient.getConfirmedRequest(eventIds);
        } else {
            confirmedRequestsMap = Collections.emptyMap();
        }
        return confirmedRequestsMap;
    }
}

