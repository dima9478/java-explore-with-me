package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
    List<Event> findAllByCategoryId(long catId);

    @EntityGraph("Event.eager")
    List<Event> findAllByAuthorId(long authorId, Pageable pageable);

    Optional<Event> findByIdAndAuthorId(long eventId, long userId);

    @EntityGraph("Event.eager")
    List<Event> findAllByIdIn(List<Long> eventIds);

    Optional<Event> findByIdAndState(long eventId, EventState state);

    @Query("select e " +
            "from Event e " +
            "where e.state = ?1 and function('distance', e.location.lat, e.location.lon, ?2, ?3) <= ?4 "
    )
    List<Event> findByPoiAndState(EventState state, double poiLat, double poiLon, double poiRadius);
}
