package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.model.EventSimilarity;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {
    Optional<EventSimilarity> findByEventIdAAndEventIdB(Long eventIdA, Long eventIdB);
    @Query("""
        select es
        from EventSimilarity es
        left join UserAction ua on ua.eventId = es.eventIdA and ua.userId = :userId
        where ua.userId is null and es.eventIdA = :eventId
        order by es.score desc
        """)
    List<EventSimilarity> findUnseenSimilarEventsForUser(
            @Param("userId") Long userId,
            @Param("eventId") Long eventId
    );
}
