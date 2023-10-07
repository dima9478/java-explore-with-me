package ru.practicum.ewm.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.ewm.common.dto.ParticipationRequestDto;
import ru.practicum.ewm.common.error.ConflictException;
import ru.practicum.ewm.common.model.*;
import ru.practicum.ewm.common.param.PaginationRequest;
import ru.practicum.ewm.common.param.PaginationRequestConverter;
import ru.practicum.ewm.common.repository.EventRepository;
import ru.practicum.ewm.common.repository.RequestRepository;
import ru.practicum.ewm.common.util.DbAvailabilityChecker;
import ru.practicum.ewm.common.util.EventDtoAuxiliaryProcessor;
import ru.practicum.ewm.users.dto.*;
import ru.practicum.ewm.users.mapper.EventMapper;
import ru.practicum.ewm.users.mapper.RequestMapper;

import javax.validation.Valid;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Validated
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final DbAvailabilityChecker availabilityChecker;
    private final EventDtoAuxiliaryProcessor eventProcessor;

    @Override
    public EventFullDto createEvent(long userId, @Valid NewEventDto dto) {
        checkEventDate(dto.getEventDate());
        User author = availabilityChecker.checkUser(userId);
        Category category = availabilityChecker.checkCategory(dto.getCategory());

        Event event = eventRepository.save(EventMapper.toEvent(dto, author, category));
        return EventMapper.toEventFullDto(event, 0L, 0L);
    }

    @Override
    public List<EventShortDto> getUserEvents(long userId, @Valid PaginationRequest pagination) {
        List<Event> events = eventRepository.findAllByAuthorId(
                userId,
                PaginationRequestConverter.toPageable(pagination, Sort.by(Sort.Direction.ASC, "id")));
        if (events.isEmpty()) {
            return List.of();
        }

        return EventMapper.toShortDtoList(
                events,
                eventProcessor.getConfirmedRequestsMap(events),
                eventProcessor.getViewStats(events)
        );
    }

    @Override
    public EventFullDto getUserEvent(long userId, long eventId) {
        Event event = availabilityChecker.checkEventByAuthor(eventId, userId);

        return EventMapper.toEventFullDto(
                event,
                requestRepository.getEventConfirmedRequestsCount(eventId).getConfirmedRequests(),
                eventProcessor.getViewStats(List.of(event)).getOrDefault(eventId, 0L)
        );
    }

    @Override
    public EventFullDto changeUserEvent(long userId, long eventId, @Valid UpdateEventUserRequest dto) {
        checkEventDate(dto.getEventDate());
        Event event = availabilityChecker.checkEventByAuthor(eventId, userId);
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException(
                    "For the requested operation the conditions are not met.",
                    "Only pending or canceled events can be changed"
            );
        }

        event = applyPatch(event, dto);
        return EventMapper.toEventFullDto(
                event,
                requestRepository.getEventConfirmedRequestsCount(eventId).getConfirmedRequests(),
                eventProcessor.getViewStats(List.of(event)).getOrDefault(eventId, 0L)
        );
    }

    @Override
    public List<ParticipationRequestDto> getUserEventRequests(long userId, long eventId) {
        availabilityChecker.checkEventByAuthor(eventId, userId);

        return RequestMapper.toDtoList(requestRepository.getAllByEventId(eventId));
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestsState(long userId, long eventId, EventRequestStatusUpdateRequest dto) {
        Event event = availabilityChecker.checkEventByAuthor(eventId, userId);

        Integer participantLimit = event.getParticipantLimit();
        if (!(!event.isRequestModeration() || participantLimit == 0)) {
            Long confirmedRequestsCount = requestRepository
                    .getEventConfirmedRequestsCount(eventId)
                    .getConfirmedRequests();

            Long delta = dto.getStatus().equals(UpdateRequestStatus.CONFIRMED) ? dto.getRequestIds().size() : 0L;
            if (confirmedRequestsCount + delta > participantLimit) {
                throw new ConflictException(
                        "For the requested operation the conditions are not met.",
                        "The participant limit has been reached"
                );
            }

            List<Request> requests = requestRepository.findAllByIdIn(dto.getRequestIds());
            if (requests.stream()
                    .anyMatch(r -> !r.getStatus().equals(RequestStatus.PENDING))) {
                throw new ConflictException(
                        "Incorrectly made request.",
                        "Request must have status PENDING"
                );
            }

            for (Request request : requests) {
                request.setStatus(RequestStatus.valueOf(dto.getStatus().name()));
            }

            if (confirmedRequestsCount + delta == participantLimit) {
                for (Request request : requestRepository.getAllByEventIdAndStatus(eventId, RequestStatus.PENDING)) {
                    request.setStatus(RequestStatus.REJECTED);
                }
            }
        }

        List<Request> confirmedRequests = requestRepository.getAllByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        List<Request> rejectedRequests = requestRepository.getAllByEventIdAndStatus(eventId, RequestStatus.REJECTED);

        return EventRequestStatusUpdateResult.builder()
                .rejectedRequests(RequestMapper.toDtoList(confirmedRequests))
                .confirmedRequests(RequestMapper.toDtoList(rejectedRequests))
                .build();
    }

    private void checkEventDate(LocalDateTime date) {
        if (date.isBefore(LocalDateTime.now().plusDays(2L))) {
            throw new ConflictException(
                    "For the requested operation the conditions are not met.",
                    String.format("Field: eventDate. Error: должно содержать дату, которая еще не наступила." +
                            " Value: %s", date)
            );
        }
    }

    private Event applyPatch(Event event, UpdateEventUserRequest dto) {
        Set<String> skipFields = Set.of("category", "stateAction");

        Category category = availabilityChecker.checkCategory(dto.getCategory());
        event.setCategory(category);
        switch (dto.getStateAction()) {
            case CANCEL_REVIEW:
                event.setState(EventState.CANCELED);
                break;
            case SEND_TO_REVIEW:
                event.setState(EventState.PENDING);
        }

        return copyNonNullAttrs(event, dto, skipFields);
    }

    private Event copyNonNullAttrs(Event event, UpdateEventUserRequest dto, Set<String> skipFields) {
        Map<String, Field> eventFields = Arrays.stream(Event.class.getDeclaredFields())
                .peek(f -> f.setAccessible(true))
                .collect(Collectors.toMap(Field::getName, Function.identity()));

        for (Field field : UpdateEventUserRequest.class.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            if (skipFields.contains(fieldName)) {
                continue;
            }

            try {
                if (field.get(dto) != null) {
                    eventFields.get(fieldName).set(event, field.get(dto));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        return event;
    }
}
