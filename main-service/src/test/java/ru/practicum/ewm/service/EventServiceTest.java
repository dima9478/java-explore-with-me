package ru.practicum.ewm.service;

import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.error.BadRequestException;
import ru.practicum.ewm.error.ConflictException;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.param.EventSort;
import ru.practicum.ewm.param.ExtendedEventFilter;
import ru.practicum.ewm.param.PaginationRequest;
import ru.practicum.ewm.param.PublicEventFilter;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.util.DbAvailabilityChecker;
import ru.practicum.ewm.util.EventDtoAuxiliaryProcessor;
import ru.practicum.ewm.util.EventHits;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {
    @Mock
    private EventRepository eventRepository;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private DbAvailabilityChecker availabilityChecker;
    @Mock
    private EventDtoAuxiliaryProcessor eventProcessor;
    @InjectMocks
    private EventServiceImpl service;
    private Event event;
    private User user;
    private Request request;
    private Category category;

    @BeforeEach
    void setUp() {
        user = User.builder().email("em@c.ru").name("uname").id(1L).build();
        category = Category.builder().name("cname").id(1L).build();
        event = Event.builder()
                .id(1L)
                .eventDate(LocalDateTime.now())
                .annotation("ann1")
                .category(category)
                .author(user)
                .createdOn(LocalDateTime.now())
                .description("desc1")
                .paid(true)
                .participantLimit(0)
                .title("title1")
                .build();
        request = Request.builder()
                .id(1L)
                .event(event)
                .requestor(User.builder().id(2L).build())
                .status(RequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void createEvent() {
        NewEventDto createDto = NewEventDto.builder()
                .eventDate(LocalDateTime.now().plusDays(2))
                .paid(true)
                .title("title")
                .category(1L)
                .requestModeration(true)
                .build();
        when(availabilityChecker.checkUser(1L)).thenReturn(user);
        when(availabilityChecker.checkCategory(1L)).thenReturn(category);
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventFullDto dto = service.createEvent(1L, createDto);

        assertThat(dto.getPublishedOn(), nullValue());
        assertThat(dto.getConfirmedRequests(), equalTo(0L));
        assertThat(dto.getViews(), equalTo(0L));
        assertThat(dto.getId(), equalTo(1L));
    }

    @Test
    void createEvent_whenInvalidDate_thenThrowBadRequest() {
        NewEventDto createDto = NewEventDto.builder()
                .eventDate(LocalDateTime.now().minusDays(2))
                .paid(true)
                .title("title")
                .category(1L)
                .requestModeration(true)
                .build();

        assertThrows(BadRequestException.class, () -> service.createEvent(1L, createDto));
    }

    @Test
    void getUserEvents() {
        when(eventProcessor.getViewStats(any(List.class))).thenReturn(Map.of(1L, 100L));
        when(eventProcessor.getConfirmedRequestsMap(any(List.class))).thenReturn(Map.of(1L, 5L));
        when(eventRepository.findAllByAuthorId(eq(1L), any(Pageable.class))).thenReturn(List.of(event));

        List<EventShortDto> events = service.getUserEvents(1L, new PaginationRequest(10, 0));

        assertThat(events.size(), equalTo(1));
        assertThat(events.get(0).getConfirmedRequests(), equalTo(5L));
    }

    @Test
    void getUserEvent() {
        when(eventProcessor.getViewStats(any(List.class))).thenReturn(Map.of(1L, 100L));
        when(requestRepository.getEventConfirmedRequestsCount(1L)).thenReturn(5L);
        when(availabilityChecker.checkEventByAuthor(1L, 1L)).thenReturn(event);


        EventFullDto dto = service.getUserEvent(1L, 1L);

        assertThat(dto.getId(), equalTo(event.getId()));
        assertThat(dto.getConfirmedRequests(), equalTo(5L));
    }

    @Test
    void changeUserEvent_whenPublished_thenThrowConflict() {
        UpdateEventUserRequest updateDto = UpdateEventUserRequest.builder().build();
        event.setState(EventState.PUBLISHED);
        when(availabilityChecker.checkEventByAuthor(1L, 1L)).thenReturn(event);

        assertThrows(ConflictException.class, () -> service.changeUserEvent(1L, 1L, updateDto));
    }

    @Test
    void changeUserEvent_whenSendToReview_thenPendingStatus() {
        UpdateEventUserRequest updateDto = UpdateEventUserRequest.builder()
                .stateAction(EventStateAction.SEND_TO_REVIEW).build();
        event.setState(EventState.CANCELED);
        when(availabilityChecker.checkEventByAuthor(1L, 1L)).thenReturn(event);

        EventFullDto dto = service.changeUserEvent(1L, 1L, updateDto);

        assertThat(dto.getState(), equalTo(EventState.PENDING));
    }

    @Test
    void getUserEventRequests() {
        when(requestRepository.getAllByEventId(1L)).thenReturn(List.of(request));

        List<ParticipationRequestDto> requests = service.getUserEventRequests(1L, 1L);

        verify(availabilityChecker, times(1)).checkEventByAuthor(1L, 1L);
        assertThat(requests.size(), equalTo(1));
    }

    @Test
    void changeRequestsState_whenEventAutoConfirmed_thenThrowConflict() {
        EventRequestStatusUpdateRequest updateDto = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(1L))
                .status(UpdateRequestStatus.CONFIRMED)
                .build();
        when(availabilityChecker.checkEventByAuthor(1L, 1L)).thenReturn(event);

        assertThrows(ConflictException.class, () -> service.changeRequestsState(1L, 1L, updateDto));
    }

    @Test
    void changeRequestsState() {
        event.setParticipantLimit(100);
        event.setRequestModeration(true);
        EventRequestStatusUpdateRequest updateDto = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(1L))
                .status(UpdateRequestStatus.CONFIRMED)
                .build();
        when(availabilityChecker.checkEventByAuthor(1L, 1L)).thenReturn(event);
        when(requestRepository.getAllByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(List.of(request));
        when(requestRepository.getAllByEventIdAndStatus(1L, RequestStatus.REJECTED)).thenReturn(List.of());
        when(requestRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(request));
        when(requestRepository.getEventConfirmedRequestsCount(1L)).thenReturn(90L);

        EventRequestStatusUpdateResult dto = service.changeRequestsState(1L, 1L, updateDto);
        verify(requestRepository, atLeast(1)).save(any(Request.class));
        assertThat(dto.getConfirmedRequests().size(), equalTo(1));
        assertThat(dto.getRejectedRequests(), empty());
    }

    @Test
    void getEventsByExtendedFilter() {
        ExtendedEventFilter filter = ExtendedEventFilter.builder().users(List.of(1L)).build();
        when(eventRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(event)));
        when(eventProcessor.getViewStats(any(List.class))).thenReturn(Map.of(1L, 100L));
        when(eventProcessor.getConfirmedRequestsMap(any(List.class))).thenReturn(Map.of(1L, 5L));

        List<EventFullDto> events = service.getEventsByExtendedFilter(filter, new PaginationRequest(10, 0));

        assertThat(events.size(), equalTo(1));
        assertThat(events.get(0).getConfirmedRequests(), equalTo(5L));
        assertThat(events.get(0).getViews(), equalTo(100L));
    }

    @Test
    void changeEvent_whenPublishActionAndEventNotPending_thenThrowConflict() {
        UpdateEventAdminRequest updateDto = UpdateEventAdminRequest.builder()
                .stateAction(AdminEventStateAction.PUBLISH_EVENT)
                .build();
        event.setState(EventState.PUBLISHED);
        when(availabilityChecker.checkEvent(1L)).thenReturn(event);

        assertThrows(ConflictException.class, () -> service.changeEvent(1L, updateDto));
    }

    @Test
    void changeEvent_whenRejectActionAndEventPublished_thenThrowConflict() {
        event.setState(EventState.PUBLISHED);
        UpdateEventAdminRequest updateDto = UpdateEventAdminRequest.builder()
                .stateAction(AdminEventStateAction.REJECT_EVENT)
                .build();
        when(availabilityChecker.checkEvent(1L)).thenReturn(event);

        assertThrows(ConflictException.class, () -> service.changeEvent(1L, updateDto));
    }

    @Test
    void changeEvent_whenPastDate_thenThrowBadRequest() {
        UpdateEventAdminRequest updateDto = UpdateEventAdminRequest.builder()
                .stateAction(AdminEventStateAction.REJECT_EVENT)
                .eventDate(LocalDateTime.now().minusDays(1))
                .build();
        when(availabilityChecker.checkEvent(1L)).thenReturn(event);

        assertThrows(BadRequestException.class, () -> service.changeEvent(1L, updateDto));
    }

    @Test
    void changeEvent() {
        event.setState(EventState.PENDING);
        UpdateEventAdminRequest updateDto = UpdateEventAdminRequest.builder()
                .stateAction(AdminEventStateAction.PUBLISH_EVENT)
                .eventDate(LocalDateTime.now().plusDays(1))
                .build();
        when(availabilityChecker.checkEvent(1L)).thenReturn(event);

        EventFullDto dto = service.changeEvent(1L, updateDto);

        assertThat(dto.getPublishedOn(), notNullValue());
        assertThat(dto.getState(), equalTo(EventState.PUBLISHED));
    }

    @Test
    void getEventsByPublicFilter_whenInvalidDateRanges_thenThrowBadRequest() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        PublicEventFilter filter = PublicEventFilter.builder()
                .rangeStart(LocalDateTime.now())
                .rangeEnd(LocalDateTime.now().minusDays(1))
                .build();

        assertThrows(BadRequestException.class,
                () -> service.getEventsByPublicFilter(filter, new PaginationRequest(10, 0), req));
    }

    @Test
    void getEventsByPublicFilter_whenViewsSort() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        PublicEventFilter filter = PublicEventFilter.builder()
                .sort(EventSort.VIEWS)
                .onlyAvailable(false)
                .build();
        when(eventProcessor.getViewStats(any(List.class))).thenReturn(Map.of(1L, 100L));
        when(eventProcessor.getConfirmedRequestsMap(any(List.class))).thenReturn(Map.of(1L, 5L));
        when(eventProcessor.getViewStats()).thenReturn(List.of(EventHits.builder().eventId(1L).hits(30L).build()));
        when(eventRepository.findAll(any(Predicate.class))).thenReturn(List.of(event));

        List<EventShortDto> events = service.getEventsByPublicFilter(filter, new PaginationRequest(10, 0), req);

        assertThat(events.size(), equalTo(1));
        assertThat(events.get(0).getViews(), equalTo(100L));
        verify(eventProcessor, times(1)).sendHit(any(HttpServletRequest.class));
    }

    @Test
    void getPublicEvent() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        when(availabilityChecker.checkEventByState(1L, EventState.PUBLISHED)).thenReturn(event);

        EventFullDto dto = service.getPublicEvent(1L, req);

        verify(eventProcessor, times(1)).sendHit(any(HttpServletRequest.class));
        assertThat(dto.getId(), equalTo(1L));
    }
}
