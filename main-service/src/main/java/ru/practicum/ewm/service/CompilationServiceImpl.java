package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.NewCompilationDto;
import ru.practicum.ewm.dto.UpdateCompilationRequest;
import ru.practicum.ewm.error.NotFoundException;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.param.PaginationRequest;
import ru.practicum.ewm.param.PaginationRequestConverter;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.util.DbAvailabilityChecker;
import ru.practicum.ewm.util.EventDtoAuxiliaryProcessor;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Validated
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final DbAvailabilityChecker availabilityChecker;
    private final EventDtoAuxiliaryProcessor eventProcessor;

    @Transactional
    @Override
    public CompilationDto addCompilation(@Valid NewCompilationDto dto) {
        List<Event> events;
        if (dto.getEvents() != null) {
            events = eventRepository.findAllByIdIn(new ArrayList<>(dto.getEvents()));
            if (events.size() != dto.getEvents().size()) {
                throw new NotFoundException("Object not found", "One or more event id not found");
            }
        } else {
            events = List.of();
        }

        Compilation compilation = compilationRepository.save(CompilationMapper.toCompilation(dto, events));

        return CompilationMapper.toDto(
                compilation,
                eventProcessor.getConfirmedRequestsMap(events),
                eventProcessor.getViewStats(events));
    }

    @Transactional
    @Override
    public CompilationDto changeCompilation(long compId, @Valid UpdateCompilationRequest dto) {
        Compilation compilation = availabilityChecker.checkCompilation(compId);

        applyPatch(compilation, dto);
        compilation = compilationRepository.save(compilation);

        return CompilationMapper.toDto(
                compilation,
                eventProcessor.getConfirmedRequestsMap(compilation.getEvents()),
                eventProcessor.getViewStats(compilation.getEvents()));
    }

    @Transactional
    @Override
    public void deleteCompilation(long compId) {
        availabilityChecker.checkCompilation(compId);

        compilationRepository.deleteById(compId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CompilationDto> getCompilations(boolean pinned, @Valid PaginationRequest pag) {
        List<Compilation> compilations = compilationRepository.findAllByPinned(
                pinned,
                PaginationRequestConverter.toPageable(pag, Sort.by(Sort.Direction.ASC, "id")));

        Set<Event> eventsSet = new HashSet<>();
        for (Compilation compilation : compilations) {
            eventsSet.addAll(compilation.getEvents());
        }
        List<Event> events = new ArrayList<>(eventsSet);

        return CompilationMapper.toDtoList(
                compilations,
                eventProcessor.getConfirmedRequestsMap(events),
                eventProcessor.getViewStats(events)
        );
    }

    @Transactional(readOnly = true)
    @Override
    public CompilationDto getCompilation(long compId) {
        Compilation compilation = availabilityChecker.checkCompilation(compId);

        return CompilationMapper.toDto(
                compilation,
                eventProcessor.getConfirmedRequestsMap(compilation.getEvents()),
                eventProcessor.getViewStats(compilation.getEvents())
        );
    }

    private Compilation applyPatch(Compilation compilation, UpdateCompilationRequest dto) {
        Boolean isPinned = dto.getPinned();
        String title = dto.getTitle();
        Set<Long> eventIds = dto.getEvents();

        if (isPinned != null) {
            compilation.setPinned(isPinned);
        }
        if (title != null) {
            compilation.setTitle(title);
        }
        if (eventIds != null) {
            List<Event> events = eventRepository.findAllByIdIn(new ArrayList<>(eventIds));
            if (eventIds.size() != events.size()) {
                throw new NotFoundException("Object not found", "One or more event id not found");
            }
            compilation.setEvents(events);
        }

        return compilation;
    }
}
