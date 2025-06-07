package ru.practicum.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.View;

import java.util.List;


public interface ViewRepository extends JpaRepository<View, Long> {
    List<View> findByEventInAndIp(List<Event> events, String ip);

    boolean existsByEventIdAndIp(Long eventId, String ip);
}
