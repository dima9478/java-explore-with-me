package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.EventConfirmedRequestCount;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    @EntityGraph("Request.eager")
    List<Request> getAllByEventId(long eventId);

    List<Request> getAllByEventIdAndStatus(long eventId, RequestStatus status);

    @Query("select new ru.practicum.ewm.model.EventConfirmedRequestCount(r.event.id, count(*)) " +
            "from Request r " +
            "where r.status = 'CONFIRMED' and r.event.id in ?1 " +
            "group by r.event.id"
    )
    List<EventConfirmedRequestCount> getEventsConfirmedRequestsCount(List<Long> eventIds);

    @Query("select count(*) " +
            "from Request r " +
            "where r.status = 'CONFIRMED' and r.event.id = ?1 "
    )
    long getEventConfirmedRequestsCount(long eventId);

    @EntityGraph("Request.eager")
    List<Request> findAllByIdIn(List<Long> requestIds);

    @EntityGraph("Request.eager")
    List<Request> findByRequestorId(long requestorId);

    Optional<Request> getAllByIdAndRequestorId(long requestId, long userId);
}
