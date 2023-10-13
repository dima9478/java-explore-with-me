package ru.practicum.ewm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.error.ConflictException;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.util.DbAvailabilityChecker;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {
    @Mock
    private RequestRepository repository;
    @Mock
    private DbAvailabilityChecker availabilityChecker;
    @InjectMocks
    private RequestServiceImpl service;
    @Captor
    ArgumentCaptor<Request> captor;
    private Request request;
    private Event event;

    @BeforeEach
    void setUp() {
        event = Event.builder()
                .id(1L)
                .eventDate(LocalDateTime.now())
                .annotation("ann1")
                .category(Category.builder().name("cname").id(1L).build())
                .author(User.builder().email("em@c.ru").name("uname").id(1L).build())
                .createdOn(LocalDateTime.now())
                .description("desc1")
                .paid(true)
                .participantLimit(0)
                .title("title1")
                .state(EventState.PENDING)
                .build();
        request = Request.builder()
                .id(1L)
                .event(event)
                .requestor(User.builder().email("emkl@c.ru").name("uname2").id(2L).build())
                .created(LocalDateTime.now())
                .status(RequestStatus.PENDING)
                .build();
    }

    @Test
    void addRequest_whenRequestorIsInitiator_thenThrowConflict() {
        request.getRequestor().setId(1L);
        request.getEvent().setState(EventState.PUBLISHED);
        when(availabilityChecker.checkEvent(1L)).thenReturn(event);
        when(repository.getEventRequestsCountByStatus(1L, RequestStatus.CONFIRMED)).thenReturn(2L);

        assertThrows(ConflictException.class, () -> service.addRequest(1L, 1L));
    }

    @Test
    void addRequest_whenEventNotPublished_thenThrowConflict() {
        request.getEvent().setState(EventState.CANCELED);
        when(availabilityChecker.checkEvent(1L)).thenReturn(event);
        when(repository.getEventRequestsCountByStatus(1L, RequestStatus.CONFIRMED)).thenReturn(2L);

        assertThrows(ConflictException.class, () -> service.addRequest(2L, 1L));
    }

    @Test
    void addRequest_whenRequestsCountOverLimit_thenThrowConflict() {
        request.getEvent().setState(EventState.PUBLISHED);
        request.getEvent().setParticipantLimit(2);
        when(availabilityChecker.checkEvent(1L)).thenReturn(event);
        when(repository.getEventRequestsCountByStatus(1L, RequestStatus.CONFIRMED)).thenReturn(2L);

        assertThrows(ConflictException.class, () -> service.addRequest(2L, 1L));
    }

    @Test
    void addRequest_NoLimit_thenSetConfirmed() {
        request.getEvent().setState(EventState.PUBLISHED);
        when(availabilityChecker.checkEvent(1L)).thenReturn(event);
        when(repository.getEventRequestsCountByStatus(1L, RequestStatus.CONFIRMED)).thenReturn(2L);
        when(repository.save(any(Request.class))).thenReturn(request);

        service.addRequest(2L, 1L);

        verify(repository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getStatus(), equalTo(RequestStatus.CONFIRMED));
    }

    @Test
    void getUserRequests() {
        when(repository.findByRequestorId(1L)).thenReturn(List.of(request));

        List<ParticipationRequestDto> requests = service.getUserRequests(1L);

        assertThat(requests.size(), equalTo(1));
        verify(availabilityChecker, times(1)).checkUser(1L);
    }

    @Test
    void cancelRequest() {
        when(availabilityChecker.checkRequestByRequestor(1L, 1L)).thenReturn(request);

        ParticipationRequestDto dto = service.cancelRequest(1L, 1L);

        verify(availabilityChecker, times(1)).checkUser(1L);
        assertThat(dto.getStatus(), equalTo(RequestStatus.CANCELED.name()));
    }
}
