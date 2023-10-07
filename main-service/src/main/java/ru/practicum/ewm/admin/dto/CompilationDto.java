package ru.practicum.ewm.admin.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.users.dto.EventShortDto;

import java.util.List;

@Data
@Builder
public class CompilationDto {
    private Long id;
    private List<EventShortDto> events;
    private boolean pinned;
    private String title;
}
