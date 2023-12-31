package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.NewCompilationDto;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {
    public static Compilation toCompilation(NewCompilationDto dto,
                                            List<Event> events) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned())
                .events(events)
                .build();
    }

    public static CompilationDto toDto(Compilation compilation,
                                       Map<Long, Long> confirmedRequests,
                                       Map<Long, Long> views) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.isPinned())
                .title(compilation.getTitle())
                .events(EventMapper.toShortDtoList(compilation.getEvents(), confirmedRequests, views))
                .build();
    }

    public static List<CompilationDto> toDtoList(List<Compilation> compilations,
                                                 Map<Long, Long> confirmedRequests,
                                                 Map<Long, Long> views) {
        return compilations.stream()
                .map(c -> toDto(c, confirmedRequests, views))
                .collect(Collectors.toList());
    }
}
