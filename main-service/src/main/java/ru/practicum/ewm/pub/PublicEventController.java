package ru.practicum.ewm.pub;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.common.param.EventSort;
import ru.practicum.ewm.common.param.PaginationRequest;
import ru.practicum.ewm.common.param.PublicEventFilter;
import ru.practicum.ewm.common.service.EventService;
import ru.practicum.ewm.users.dto.EventFullDto;
import ru.practicum.ewm.users.dto.EventShortDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService service;

    @GetMapping
    List<EventShortDto> getPublicEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) EventSort sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        PublicEventFilter filter = PublicEventFilter.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .build();

        return service.getEventsByPublicFilter(filter, new PaginationRequest(size, from), request);
    }

    @GetMapping("/{id}")
    EventFullDto getPublicEvent(@PathVariable long id, HttpServletRequest request) {
        return service.getPublicEvent(id, request);
    }
}
