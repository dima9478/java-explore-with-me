package ru.practicum.ewm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.NewCompilationDto;
import ru.practicum.ewm.dto.UpdateCompilationRequest;
import ru.practicum.ewm.error.NotFoundException;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.param.PaginationRequest;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.util.DbAvailabilityChecker;
import ru.practicum.ewm.util.EventDtoAuxiliaryProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompilationServiceTest {
    @Mock
    private CompilationRepository compilationRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private DbAvailabilityChecker availabilityChecker;
    @Mock
    private EventDtoAuxiliaryProcessor eventProcessor;
    @InjectMocks
    private CompilationServiceImpl service;
    private Compilation compilation;
    private Event event;
    @Captor
    private ArgumentCaptor<Compilation> captor;

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
                .build();
        compilation = Compilation.builder()
                .id(1L)
                .events(List.of())
                .pinned(true)
                .title("compTitle1")
                .build();
    }

    @Test
    void addCompilation() {
        NewCompilationDto createDto = NewCompilationDto.builder()
                .events(null)
                .pinned(true)
                .title("compTitle1")
                .build();
        when(eventProcessor.getViewStats(any(List.class))).thenReturn(Map.of(1L, 100L));
        when(eventProcessor.getConfirmedRequestsMap(any(List.class))).thenReturn(Map.of(1L, 5L));
        when(compilationRepository.save(any(Compilation.class))).thenReturn(compilation);

        CompilationDto dto = service.addCompilation(createDto);

        verify(compilationRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getEvents(), empty());
        assertThat(dto.getEvents(), empty());
    }

    @Test
    void addCompilation_whenNotAllEventsPresented_thenThrowNotFound() {
        NewCompilationDto createDto = NewCompilationDto.builder()
                .events(Set.of(1L))
                .pinned(true)
                .title("compTitle1")
                .build();
        when(eventRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> service.addCompilation(createDto));
    }

    @Test
    void changeCompilation() {
        UpdateCompilationRequest updateDto = UpdateCompilationRequest.builder()
                .title("newTitle")
                .build();

        when(availabilityChecker.checkCompilation(1L)).thenReturn(compilation);
        when(eventProcessor.getViewStats(any(List.class))).thenReturn(Map.of(1L, 100L));
        when(eventProcessor.getConfirmedRequestsMap(any(List.class))).thenReturn(Map.of(1L, 5L));
        when(compilationRepository.save(any(Compilation.class))).thenReturn(compilation);

        CompilationDto dto = service.changeCompilation(1L, updateDto);

        verify(compilationRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getTitle(), equalTo("newTitle"));
    }

    @Test
    void deleteCompilation() {
        service.deleteCompilation(1L);

        verify(availabilityChecker, times(1)).checkCompilation(1L);
        verify(compilationRepository, times(1)).deleteById(1L);
    }

    @Test
    void getCompilations() {
        compilation.setEvents(List.of(event));
        when(compilationRepository.findAllByPinned(eq(true), any(Pageable.class)))
                .thenReturn(List.of(compilation));
        when(eventProcessor.getViewStats(any(List.class))).thenReturn(Map.of(1L, 100L));
        when(eventProcessor.getConfirmedRequestsMap(any(List.class))).thenReturn(Map.of(1L, 5L));

        List<CompilationDto> compilations = service.getCompilations(true, new PaginationRequest(10, 0));

        assertThat(compilations.size(), equalTo(1));
        assertThat(compilations.get(0).getEvents().get(0).getConfirmedRequests(), equalTo(5L));

    }

    @Test
    void getCompilation() {
        when(availabilityChecker.checkCompilation(1L)).thenReturn(compilation);
        when(eventProcessor.getViewStats(any(List.class))).thenReturn(Map.of(1L, 100L));
        when(eventProcessor.getConfirmedRequestsMap(any(List.class))).thenReturn(Map.of(1L, 5L));

        CompilationDto dto = service.getCompilation(1L);

        assertThat(dto.isPinned(), equalTo(compilation.isPinned()));
        assertThat(dto.getTitle(), equalTo(compilation.getTitle()));
    }
}
