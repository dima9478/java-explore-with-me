package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dto.ViewStats;
import ru.practicum.ewm.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {
    @Query("select new ru.practicum.dto.ViewStats(eh.app, eh.uri, count(*)) from EndpointHit eh" +
            " where eh.timestamp >= ?1 and eh.timestamp <= ?2" +
            " group by eh.app, eh.uri" +
            " order by count(*) desc")
    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.dto.ViewStats(eh.app, eh.uri, count(*)) from EndpointHit eh" +
            " where eh.timestamp >= ?1 and eh.timestamp <= ?2" +
            " and eh.uri in ?3" +
            " group by eh.app, eh.uri" +
            " order by count(*) desc")
    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.dto.ViewStats(eh.app, eh.uri, count(distinct eh.ip)) from EndpointHit eh" +
            " where eh.timestamp >= ?1 and eh.timestamp <= ?2" +
            " group by eh.app, eh.uri" +
            " order by count(*) desc")
    List<ViewStats> getIpUniqueStats(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.dto.ViewStats(eh.app, eh.uri, count(distinct eh.ip)) from EndpointHit eh" +
            " where eh.timestamp >= ?1 and eh.timestamp <= ?2" +
            " and eh.uri in ?3" +
            " group by eh.app, eh.uri" +
            " order by count(*) desc")
    List<ViewStats> getIpUniqueStats(LocalDateTime start, LocalDateTime end, List<String> uris);
}
