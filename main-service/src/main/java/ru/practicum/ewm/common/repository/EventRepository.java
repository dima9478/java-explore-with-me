package ru.practicum.ewm.common.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.common.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByCategoryId(long catId);

    @EntityGraph("Event.eager")
    List<Event> findAllByAuthorId(long authorId, Pageable pageable);

    Optional<Event> findByIdAndAuthorId(long eventId, long userId);

    @EntityGraph("Event.eager")
    List<Event> findAllByIdIn(List<Long> eventIds);
}
