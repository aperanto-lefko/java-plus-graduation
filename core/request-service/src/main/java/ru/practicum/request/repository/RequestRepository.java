package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.request.model.Request;
import ru.practicum.request.dto.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {


    List<Request> findAllByUserId(long userId);


    List<Request> findAllByUserIdAndEventId(long userId, long eventId);

    @Query("select count(r) from Request r where r.eventId = :eventId and r.status = 'CONFIRMED'")
    int findCountOfConfirmedRequestsByEventId(long eventId);


    @Modifying
    @Transactional
    @Query("UPDATE Request r SET r.status = :status WHERE r.id IN :ids")
    void updateStatus(RequestStatus status, List<Long> ids);
}