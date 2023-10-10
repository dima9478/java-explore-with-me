package ru.practicum.ewm.users.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.common.dto.ParticipationRequestDto;
import ru.practicum.ewm.common.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UsersRequestController {
    private final RequestService service;

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    ParticipationRequestDto postRequest(@PathVariable long userId,
                                        @RequestParam long eventId) {
        return service.addRequest(userId, eventId);
    }

    @GetMapping("/{userId}/requests")
    List<ParticipationRequestDto> getUserRequests(@PathVariable long userId) {
        return service.getUserRequests(userId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    ParticipationRequestDto cancelRequest(@PathVariable long userId,
                                          @PathVariable long requestId) {
        return service.cancelRequest(userId, requestId);
    }
}
