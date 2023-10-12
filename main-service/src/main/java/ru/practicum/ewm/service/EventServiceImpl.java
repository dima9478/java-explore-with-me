package ru.practicum.ewm.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.error.BadRequestException;
import ru.practicum.ewm.error.ConflictException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.param.ExtendedEventFilter;
import ru.practicum.ewm.param.PaginationRequest;
import ru.practicum.ewm.param.PaginationRequestConverter;
import ru.practicum.ewm.param.PublicEventFilter;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.util.DbAvailabilityChecker;
import ru.practicum.ewm.util.EventDtoAuxiliaryProcessor;
import ru.practicum.ewm.util.EventHits;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
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

    @Transactional
    @Override
    public EventFullDto createEvent(long userId, @Valid NewEventDto dto) {
        checkEventDate(dto.getEventDate());
        User author = availabilityChecker.checkUser(userId);
        Category category = availabilityChecker.checkCategory(dto.getCategory());

        Event event = eventRepository.save(EventMapper.toEvent(dto, author, category));
        return EventMapper.toEventFullDto(event, 0L, 0L);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getUserEvents(long userId, @Valid PaginationRequest pagination) {
        List<Event> events = eventRepository.findAllByAuthorId(
                userId,
                PaginationRequestConverter.toPageable(pagination));
        if (events.isEmpty()) {
            return List.of();
        }

        return EventMapper.toShortDtoList(
                events,
                eventProcessor.getConfirmedRequestsMap(events),
                eventProcessor.getViewStats(events)
        );
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getUserEvent(long userId, long eventId) {
        Event event = availabilityChecker.checkEventByAuthor(eventId, userId);

        return EventMapper.toEventFullDto(
                event,
                requestRepository.getEventRequestsCountByStatus(eventId, RequestStatus.CONFIRMED),
                eventProcessor.getViewStats(List.of(event)).getOrDefault(eventId, 0L)
        );
    }

    @Transactional
    @Override
    public EventFullDto changeUserEvent(long userId, long eventId, @Valid UpdateEventUserRequest dto) {
        if (dto.getEventDate() != null) {
            checkEventDate(dto.getEventDate());
        }

        Event event = availabilityChecker.checkEventByAuthor(eventId, userId);
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException(
                    "For the requested operation the conditions are not met.",
                    "Only pending or canceled events can be changed"
            );
        }

        event = applyPatch(event, dto);
        eventRepository.save(event);
        return EventMapper.toEventFullDto(
                event,
                requestRepository.getEventRequestsCountByStatus(eventId, RequestStatus.CONFIRMED),
                eventProcessor.getViewStats(List.of(event)).getOrDefault(eventId, 0L)
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getUserEventRequests(long userId, long eventId) {
        availabilityChecker.checkEventByAuthor(eventId, userId);

        return RequestMapper.toDtoList(requestRepository.getAllByEventId(eventId));
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult changeRequestsState(long userId, long eventId,
                                                              @Valid EventRequestStatusUpdateRequest dto) {
        Event event = availabilityChecker.checkEventByAuthor(eventId, userId);

        Integer participantLimit = event.getParticipantLimit();
        if (!event.isRequestModeration() || participantLimit == 0) {
            throw new ConflictException("Cannot change state of requests", "All requests were automatically confirmed");
        }

        List<Request> requests = requestRepository.findAllByIdIn(dto.getRequestIds());
        Long confirmedRequestsCount = requestRepository.getEventRequestsCountByStatus(eventId, RequestStatus.CONFIRMED);
        Long delta = dto.getStatus().equals(UpdateRequestStatus.CONFIRMED) ? dto.getRequestIds().size() : 0L;

        validateRequestUpdate(dto, requests, confirmedRequestsCount, participantLimit);

        for (Request request : requests) {
            request.setStatus(RequestStatus.valueOf(dto.getStatus().name()));
            requestRepository.save(request);
        }

        if (confirmedRequestsCount + delta == participantLimit) {
            for (Request request : requestRepository.getAllByEventIdAndStatus(eventId, RequestStatus.PENDING)) {
                request.setStatus(RequestStatus.REJECTED);
                requestRepository.save(request);
            }
        }

        List<Request> confirmedRequests = requestRepository.getAllByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        List<Request> rejectedRequests = requestRepository.getAllByEventIdAndStatus(eventId, RequestStatus.REJECTED);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(RequestMapper.toDtoList(confirmedRequests))
                .rejectedRequests(RequestMapper.toDtoList(rejectedRequests))
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> getEventsByExtendedFilter(ExtendedEventFilter filters, PaginationRequest pag) {
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();

        if (filters.getUsers() != null) {
            conditions.add(event.author.id.in(filters.getUsers()));
        }
        if (filters.getStates() != null) {
            conditions.add(event.state.in(filters.getStates()));
        }
        if (filters.getCategories() != null) {
            conditions.add(event.category.id.in(filters.getCategories()));
        }
        if (filters.getRangeStart() != null) {
            conditions.add(event.eventDate.goe(filters.getRangeStart()));
        }
        if (filters.getRangeEnd() != null) {
            conditions.add(event.eventDate.loe(filters.getRangeEnd()));
        }

        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .orElseGet(Expressions.TRUE::isTrue);

        List<Event> events = eventRepository.findAll(
                finalCondition,
                PaginationRequestConverter.toPageable(pag)).toList();

        return EventMapper.toEventFullDtoList(
                events,
                eventProcessor.getConfirmedRequestsMap(events),
                eventProcessor.getViewStats(events));
    }

    @Transactional
    @Override
    public EventFullDto changeEvent(long eventId, UpdateEventAdminRequest dto) {
        Event event = availabilityChecker.checkEvent(eventId);
        validateAdminUpdate(event, dto);

        event = applyPatch(event, dto);
        AdminEventStateAction action = dto.getStateAction();
        if (action != null && dto.getStateAction().equals(AdminEventStateAction.PUBLISH_EVENT)) {
            event.setPublishedOn(LocalDateTime.now());
        }
        eventRepository.save(event);

        return EventMapper.toEventFullDto(
                event,
                requestRepository.getEventRequestsCountByStatus(eventId, RequestStatus.CONFIRMED),
                eventProcessor.getViewStats(List.of(event)).getOrDefault(eventId, 0L)
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getEventsByPublicFilter(PublicEventFilter filter,
                                                       @Valid PaginationRequest pag,
                                                       HttpServletRequest req) {
        LocalDateTime rangeStart = filter.getRangeStart();
        LocalDateTime rangeEnd = filter.getRangeEnd();
        validateDateFilters(rangeStart, rangeEnd);

        QEvent event = QEvent.event;
        QRequest request = QRequest.request;
        List<BooleanExpression> conditions = new ArrayList<>();
        conditions.add(event.state.eq(EventState.PUBLISHED));
        if (filter.getText() != null) {
            conditions.add(
                    event.annotation.containsIgnoreCase(filter.getText())
                            .or(event.description.containsIgnoreCase(filter.getText())));
        }
        if (filter.getCategories() != null) {
            conditions.add(event.category.id.in(filter.getCategories()));
        }
        if (rangeStart != null) {
            conditions.add(event.eventDate.goe(rangeStart));
        }
        if (rangeEnd != null) {
            conditions.add(event.eventDate.loe(rangeEnd));
        }
        if (filter.getPaid() != null) {
            conditions.add(event.paid.eq(filter.getPaid()));
        }
        if (filter.getOnlyAvailable()) {
            conditions.add(event.participantLimit.lt(
                    JPAExpressions.select(request.count())
                            .from(request)
                            .where(request.status.eq(RequestStatus.CONFIRMED)))
            );
        }

        List<Event> events;
        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .orElseGet(Expressions.TRUE::isTrue);
        if (filter.getSort() != null) {
            switch (filter.getSort()) {
                case VIEWS:
                    List<EventHits> hits = eventProcessor.getViewStats();
                    List<Long> eventIds = hits.stream()
                            .map(EventHits::getEventId)
                            .skip(pag.getFrom())
                            .limit(pag.getSize())
                            .collect(Collectors.toList());
                    finalCondition = finalCondition.and(event.id.in(eventIds));
                    events = new ArrayList<>((Collection<? extends Event>) eventRepository.findAll(finalCondition));
                    events.sort(
                            Comparator.comparingLong(e -> eventIds.indexOf(e.getId()))
                    );
                    break;
                case EVENT_DATE:
                    events = eventRepository.findAll(
                                    finalCondition,
                                    PaginationRequestConverter.toPageable(pag, Sort.by(Sort.Direction.DESC, "eventDate")))
                            .toList();
                    break;
                default:
                    events = List.of();
            }
        } else {
            events = eventRepository.findAll(finalCondition, PaginationRequestConverter.toPageable(pag)).toList();
        }

        eventProcessor.sendHit(req);

        return EventMapper.toShortDtoList(
                events,
                eventProcessor.getConfirmedRequestsMap(events),
                eventProcessor.getViewStats(events));

    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getPublicEvent(long eventId, HttpServletRequest request) {
        Event event = availabilityChecker.checkEventByState(eventId, EventState.PUBLISHED);

        EventFullDto dto = EventMapper.toEventFullDto(
                event,
                requestRepository.getEventRequestsCountByStatus(eventId, RequestStatus.CONFIRMED),
                eventProcessor.getViewStats(List.of(event)).getOrDefault(eventId, 0L)
        );

        eventProcessor.sendHit(request);

        return dto;
    }

    private void checkEventDate(LocalDateTime date) {
        if (date.isBefore(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusHours(2L))) {
            throw new BadRequestException(
                    "For the requested operation the conditions are not met.",
                    String.format("Field: eventDate. Error: должно содержать дату, которая еще не наступила." +
                            " Value: %s", date)
            );
        }
    }

    private void validateAdminUpdate(Event event, UpdateEventAdminRequest dto) {
        AdminEventStateAction action = dto.getStateAction();
        if (dto.getEventDate() != null && dto.getEventDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Invalid event date", "Cannot set event date to the past time");
        }
        if (action != null) {
            if (action.equals(AdminEventStateAction.PUBLISH_EVENT) && !event.getState().equals(EventState.PENDING)) {
                throw new ConflictException(
                        "For the requested operation the conditions are not met.",
                        String.format(
                                "Cannot publish the event because it's not in the right state: %s",
                                event.getState()
                        )
                );
            }
            if (action.equals(AdminEventStateAction.REJECT_EVENT) && event.getState().equals(EventState.PUBLISHED)) {
                throw new ConflictException(
                        "For the requested operation the conditions are not met.",
                        "Cannot reject the event because it's not in the right state: PUBLISHED"
                );
            }
        }
        LocalDateTime publishedOn = action != null && action.equals(AdminEventStateAction.PUBLISH_EVENT)
                ? LocalDateTime.now() : event.getPublishedOn();
        LocalDateTime eventDate = dto.getEventDate() != null ? dto.getEventDate() : event.getEventDate();
        if (publishedOn != null
                && eventDate.plusHours(1).isBefore(publishedOn)) {
            throw new ConflictException(
                    "For the requested operation the conditions are not met.",
                    "The start date of the modified event must be no earlier than an hour from the publication date"
            );
        }
    }

    private Event applyPatch(Event event, UpdateEventUserRequest dto) {
        Set<String> skipFields = Set.of("category", "stateAction");
        if (dto.getCategory() != null) {
            Category category = availabilityChecker.checkCategory(dto.getCategory());
            event.setCategory(category);
        }
        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
            }
        }

        return copyNonNullAttrs(event, dto, skipFields);
    }

    private Event applyPatch(Event event, UpdateEventAdminRequest dto) {
        Set<String> skipFields = Set.of("category", "stateAction");

        if (dto.getCategory() != null) {
            Category category = availabilityChecker.checkCategory(dto.getCategory());
            event.setCategory(category);
        }
        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case PUBLISH_EVENT:
                    event.setState(EventState.PUBLISHED);
                    break;
                case REJECT_EVENT:
                    event.setState(EventState.CANCELED);
            }
        }

        return copyNonNullAttrs(event, dto, skipFields);
    }

    private <T> Event copyNonNullAttrs(Event event, T dto, Set<String> skipFields) {
        Map<String, Field> eventFields = Arrays.stream(Event.class.getDeclaredFields())
                .peek(f -> f.setAccessible(true))
                .collect(Collectors.toMap(Field::getName, Function.identity()));

        for (Field field : dto.getClass().getDeclaredFields()) {
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

    private void validateRequestUpdate(EventRequestStatusUpdateRequest dto,
                                       List<Request> requests,
                                       Long confirmedRequestsCount,
                                       Integer participantLimit) {
        Long delta = dto.getStatus().equals(UpdateRequestStatus.CONFIRMED) ? dto.getRequestIds().size() : 0L;
        if (confirmedRequestsCount + delta > participantLimit) {
            throw new ConflictException(
                    "For the requested operation the conditions are not met.",
                    "The participant limit has been reached"
            );
        }

        if (requests.stream()
                .anyMatch(r -> !r.getStatus().equals(RequestStatus.PENDING))) {
            throw new ConflictException(
                    "Incorrectly made request.",
                    "Request must have status PENDING"
            );
        }
    }

    private void validateDateFilters(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && !rangeStart.isBefore(rangeEnd)) {
            throw new BadRequestException(
                    "Incorrect made request",
                    "rangeEnd must be after rangeStart");
        }
    }
}
