package ru.practicum.ewm.users.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.common.dto.CategoryDto;

import java.time.LocalDateTime;

@Data
@Builder
public class EventShortDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private long confirmedRequests;
    private LocalDateTime eventDate;
    private UserShortDto initiator;
    private Boolean paid;
    private String title;
    private long views;
}
