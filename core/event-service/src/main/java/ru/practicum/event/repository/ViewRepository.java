package ru.practicum.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.View;

import java.util.List;

@Repository
public interface ViewRepository extends JpaRepository<View, Long> {
    List<View> findByEventInAndIp(List<Event> events, String ip);

    boolean existsByEventIdAndIp(Long eventId, String ip);
}
