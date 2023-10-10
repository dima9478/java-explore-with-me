package ru.practicum.ewm.common.service;

import ru.practicum.ewm.admin.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.common.dto.ParticipationRequestDto;
import ru.practicum.ewm.common.param.ExtendedEventFilter;
import ru.practicum.ewm.common.param.PaginationRequest;
import ru.practicum.ewm.common.param.PublicEventFilter;
import ru.practicum.ewm.users.dto.*;

import javax.servlet.http.HttpServletRequest;
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

    List<EventFullDto> getEventsByExtendedFilter(ExtendedEventFilter filters, @Valid PaginationRequest pag);

    EventFullDto changeEvent(long eventId, @Valid UpdateEventAdminRequest dto);

    List<EventShortDto> getEventsByPublicFilter(PublicEventFilter filter,
                                                @Valid PaginationRequest pag,
                                                HttpServletRequest request);

    EventFullDto getPublicEvent(long eventId, HttpServletRequest request);
}
