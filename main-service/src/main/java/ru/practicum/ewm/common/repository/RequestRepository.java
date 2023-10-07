package ru.practicum.ewm.common.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.common.model.EventConfirmedRequestCount;
import ru.practicum.ewm.common.model.Request;
import ru.practicum.ewm.common.model.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    @EntityGraph("Request.eager")
    List<Request> getAllByEventId(long eventId);

    List<Request> getAllByEventIdAndStatus(long eventId, RequestStatus status);

    @Query("select new ru.practicum.ewm.common.model.EventConfirmedRequestCount(r.event.id, count(*)) " +
            "from Request r " +
            "where r.status = 'CONFIRMED' and r.event.id in ?1" +
            "group by r.event.id"
    )
    List<EventConfirmedRequestCount> getEventsConfirmedRequestsCount(List<Long> eventIds);

    @Query("select new ru.practicum.ewm.common.model.EventConfirmedRequestCount(r.event.id, count(*)) " +
            "from Request r " +
            "where r.status = 'CONFIRMED' and r.event.id = ?1"
    )
    EventConfirmedRequestCount getEventConfirmedRequestsCount(long eventId);

    @EntityGraph("Request.eager")
    List<Request> findAllByIdIn(List<Long> requestIds);
}
