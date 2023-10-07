package ru.practicum.ewm.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.common.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByIdIn(List<Long> ids);
}
