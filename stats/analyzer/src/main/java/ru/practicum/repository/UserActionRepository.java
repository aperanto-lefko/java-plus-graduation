package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.model.UserAction;

@Repository
public interface UserActionRepository extends JpaRepository<UserAction, Long> {
}
