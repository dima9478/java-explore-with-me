package ru.practicum.ewm.users.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.common.dto.CategoryDto;
import ru.practicum.ewm.common.model.EventState;
import ru.practicum.ewm.common.model.Location;

import java.time.LocalDateTime;

@Data
@Builder
public class EventFullDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private long confirmedRequests;
    private LocalDateTime createdOn;
    private String description;
    private LocalDateTime eventDate;
    private UserShortDto initiator;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    private EventState state;
    private String title;
    private long views;
}
