package ru.practicum.ewm.common.service;

import ru.practicum.ewm.common.dto.ParticipationRequestDto;
import ru.practicum.ewm.common.param.PaginationRequest;
import ru.practicum.ewm.users.dto.*;

import javax.validation.Valid;
import java.util.List;

public interface EventService {
    EventFullDto createEvent(long userId, @Valid NewEventDto dto);

    List<EventShortDto> getUserEvents(long userId, @Valid PaginationRequest pagination);

    EventFullDto getUserEvent(long userId, long eventId);

    EventFullDto changeUserEvent(long userId, long eventId, @Valid UpdateEventUserRequest dto);

    List<ParticipationRequestDto> getUserEventRequests(long userId, long eventId);

    EventRequestStatusUpdateResult changeRequestsState(long userId,
                                                       long eventId,
                                                       @Valid EventRequestStatusUpdateRequest dto);
}
