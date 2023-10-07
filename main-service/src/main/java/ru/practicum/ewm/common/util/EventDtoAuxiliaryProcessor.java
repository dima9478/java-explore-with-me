package ru.practicum.ewm.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.ViewStats;
import ru.practicum.ewm.common.model.Event;
import ru.practicum.ewm.common.model.EventConfirmedRequestCount;
import ru.practicum.ewm.common.repository.RequestRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventDtoAuxiliaryProcessor {
    private final StatsClient client;
    @Value("${service.name}")
    private String serviceName;
    private final RequestRepository requestRepository;

    public Map<Long, Long> getViewStats(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }
        LocalDateTime midnightToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        List<String> uris = events.stream()
                .map(e -> String.format("/events/%d", e.getId()))
                .collect(Collectors.toList());

        ResponseEntity<List<ViewStats>> resp = client.getStats(midnightToday, LocalDateTime.now(), uris, true);
        // prefer availability to consistency
        if (!resp.getStatusCode().is2xxSuccessful()) {
            return Map.of();
        }

        return resp.getBody().stream()
                .filter(s -> s.getApp().equals(serviceName))
                .collect(Collectors.toMap(
                        s -> {
                            String uri = s.getUri();
                            return Long.parseLong(uri.substring(uri.lastIndexOf("/") + 1));
                        },
                        ViewStats::getHits
                ));
    }

    public Map<Long, Long> getConfirmedRequestsMap(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        List<EventConfirmedRequestCount> confirmedRequestCounts =
                requestRepository.getEventsConfirmedRequestsCount(eventIds);

        return confirmedRequestCounts.stream()
                .collect(Collectors.toMap(
                        EventConfirmedRequestCount::getEventId,
                        EventConfirmedRequestCount::getConfirmedRequests
                ));
    }
}
