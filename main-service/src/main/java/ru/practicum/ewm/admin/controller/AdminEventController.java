package ru.practicum.ewm.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.admin.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.common.model.EventState;
import ru.practicum.ewm.common.param.ExtendedEventFilter;
import ru.practicum.ewm.common.param.PaginationRequest;
import ru.practicum.ewm.common.service.EventService;
import ru.practicum.ewm.users.dto.EventFullDto;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {
    private final EventService service;

    @GetMapping
    List<EventFullDto> getEventsByAdmin(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) EnumSet<EventState> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        ExtendedEventFilter filter = ExtendedEventFilter.builder()
                .users(users)
                .categories(categories)
                .states(states)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .build();

        return service.getEventsByExtendedFilter(filter, new PaginationRequest(size, from));
    }

    @PatchMapping("/{eventId}")
    EventFullDto changeEventByAdmin(@PathVariable long eventId,
                                    @RequestBody UpdateEventAdminRequest dto) {
        return service.changeEvent(eventId, dto);
    }
}
