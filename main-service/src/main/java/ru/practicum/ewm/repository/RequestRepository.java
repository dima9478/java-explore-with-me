package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.EventRequestCount;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    @EntityGraph("Request.eager")
    List<Request> getAllByEventId(long eventId);

    List<Request> getAllByEventIdAndStatus(long eventId, RequestStatus status);

    @Query("select new ru.practicum.ewm.model.EventRequestCount(r.event.id, count(*)) " +
            "from Request r " +
            "where r.status = ?2 and r.event.id in ?1 " +
            "group by r.event.id"
    )
    List<EventRequestCount> getEventsRequestsCountByStatus(List<Long> eventIds, RequestStatus status);

    @Query("select count(*) " +
            "from Request r " +
            "where r.status = ?2 and r.event.id = ?1 "
    )
    long getEventRequestsCountByStatus(long eventId, RequestStatus status);

    @EntityGraph("Request.eager")
    List<Request> findAllByIdIn(List<Long> requestIds);

    @EntityGraph("Request.eager")
    List<Request> findByRequestorId(long requestorId);

    Optional<Request> getAllByIdAndRequestorId(long requestId, long userId);
}
