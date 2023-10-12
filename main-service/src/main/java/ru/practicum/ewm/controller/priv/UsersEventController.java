package ru.practicum.ewm.controller.priv;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.param.PaginationRequest;
import ru.practicum.ewm.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersEventController {
    private final EventService service;

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    EventFullDto postEvent(@PathVariable long userId, @RequestBody NewEventDto dto) {
        return service.createEvent(userId, dto);
    }

    @GetMapping("/{userId}/events")
    List<EventShortDto> getUserEvents(@PathVariable long userId,
                                      @RequestParam(defaultValue = "0") int from,
                                      @RequestParam(defaultValue = "10") int size) {
        return service.getUserEvents(userId, new PaginationRequest(size, from));
    }

    @GetMapping("/{userId}/events/{eventId}")
    EventFullDto getUserEvent(@PathVariable long userId,
                              @PathVariable long eventId) {
        return service.getUserEvent(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    EventFullDto patchUserEvent(@PathVariable long userId,
                                @PathVariable long eventId,
                                @RequestBody UpdateEventUserRequest dto) {
        return service.changeUserEvent(userId, eventId, dto);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    List<ParticipationRequestDto> getUserEventRequests(@PathVariable long userId,
                                                       @PathVariable long eventId) {
        return service.getUserEventRequests(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    EventRequestStatusUpdateResult processRequestsState(@PathVariable long userId,
                                                        @PathVariable long eventId,
                                                        @RequestBody EventRequestStatusUpdateRequest dto) {
        return service.changeRequestsState(userId, eventId, dto);
    }
}
