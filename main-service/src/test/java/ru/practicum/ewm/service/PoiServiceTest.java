package ru.practicum.ewm.service;

import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.error.BadRequestException;
import ru.practicum.ewm.error.ConflictException;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.param.Bbox;
import ru.practicum.ewm.param.PaginationRequest;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.PoiRepository;
import ru.practicum.ewm.util.DbAvailabilityChecker;
import ru.practicum.ewm.util.EventDtoAuxiliaryProcessor;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PoiServiceTest {
    @Mock
    private PoiRepository poiRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private DbAvailabilityChecker availabilityChecker;
    @Mock
    private EventDtoAuxiliaryProcessor eventProcessor;
    @InjectMocks
    private PoiServiceImpl service;
    @Captor
    private ArgumentCaptor<Poi> captor;
    private Poi poi;

    @BeforeEach
    void setUp() {
        poi = Poi.builder()
                .id(1L)
                .state(PoiState.PENDING)
                .impactRadius(45)
                .location(new Location(45, 45))
                .name("poi name 1")
                .build();
    }

    @Test
    void addPoiByAdmin() {
        when(poiRepository.save(captor.capture())).thenReturn(poi);

        PoiDto dto = service.addPoiByAdmin(NewPoiRequest.builder().build());

        assertThat(captor.getValue().getState(), equalTo(PoiState.CONFIRMED));
        assertThat(dto.getId(), equalTo(1L));
    }

    @Test
    void changePoi_whenChangeStateOfNonPendingPoi_thenThrowConflict() {
        UpdatePoiRequest dto = UpdatePoiRequest.builder()
                .action(PoiStateAction.CONFIRM_POI)
                .build();
        poi.setState(PoiState.REJECTED);
        when(availabilityChecker.checkPoi(1L)).thenReturn(poi);

        assertThrows(ConflictException.class, () -> service.changePoi(1L, dto));
    }

    @Test
    void changePoi() {
        UpdatePoiRequest dto = UpdatePoiRequest.builder()
                .action(PoiStateAction.CONFIRM_POI)
                .name("new name")
                .build();
        when(availabilityChecker.checkPoi(1L)).thenReturn(poi);
        when(poiRepository.save(captor.capture())).thenReturn(poi);

        service.changePoi(1L, dto);

        Poi savedPoi = captor.getValue();
        assertThat(savedPoi.getName(), equalTo("new name"));
        assertThat(savedPoi.getState(), equalTo(PoiState.CONFIRMED));
    }

    @Test
    void addPoiByUser() {
        when(poiRepository.save(captor.capture())).thenReturn(poi);

        PoiDto dto = service.addPoiByUser(NewPoiRequest.builder().build());

        assertThat(captor.getValue().getState(), equalTo(PoiState.PENDING));
        assertThat(dto.getId(), equalTo(1L));
    }

    @Test
    void getPois_whenInvalidBbox_thenThrowBadRequest() {
        assertThrows(BadRequestException.class,
                () -> service.getPois(
                        null,
                        new Bbox(-190, 90, 34, 34),
                        new PaginationRequest(10, 0)));
    }

    @Test
    void getPois() {
        when(poiRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(poi)));

        List<PoiShortDto> pois = service.getPois(
                null,
                new Bbox(-45, -56, 45, 34),
                new PaginationRequest(10, 0));

        assertThat(pois.size(), equalTo(1));
        assertThat(pois.get(0).getId(), equalTo(1L));
        assertThat(pois.get(0).getName(), equalTo("poi name 1"));
    }

    @Test
    void getPoisByAdmin_whenInvalidBbox_thenThrowBadRequest() {
        assertThrows(BadRequestException.class,
                () -> service.getPoisByAdmin(
                        new Bbox(-190, 90, 34, 34),
                        null,
                        new PaginationRequest(10, 0)));
    }

    @Test
    void getPoisByAdmin() {
        when(poiRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(poi)));

        List<PoiDto> pois = service.getPoisByAdmin(
                new Bbox(-45, -56, 45, 34),
                null,
                new PaginationRequest(10, 0));

        assertThat(pois.size(), equalTo(1));
        assertThat(pois.get(0).getId(), equalTo(1L));
        assertThat(pois.get(0).getName(), equalTo("poi name 1"));
    }

    @Test
    void getPoiEvents() {
        Event event = Event.builder()
                .id(1L)
                .eventDate(LocalDateTime.now())
                .annotation("ann1")
                .createdOn(LocalDateTime.now())
                .category(new Category(1L, "cat name"))
                .author(new User(1L, "name", "email"))
                .description("desc1")
                .paid(true)
                .participantLimit(0)
                .title("title1")
                .build();
        when(availabilityChecker.checkPoi(1L)).thenReturn(poi);
        when(eventRepository.findByPoiAndState(EventState.PUBLISHED, 45, 45, 45))
                .thenReturn(List.of(event));

        List<EventShortDto> events = service.getPoiEvents(1L);

        assertThat(events.size(), equalTo(1));
        assertThat(events.get(0).getAnnotation(), equalTo("ann1"));
    }
}
