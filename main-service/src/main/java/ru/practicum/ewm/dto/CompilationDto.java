package ru.practicum.ewm.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CompilationDto {
    private Long id;
    private List<EventShortDto> events;
    private boolean pinned;
    private String title;
}
