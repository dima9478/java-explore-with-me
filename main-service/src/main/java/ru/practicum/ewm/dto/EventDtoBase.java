package ru.practicum.ewm.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@SuperBuilder
public class EventDtoBase {
    private Long id;
    @NotBlank
    private String annotation;
    @NotNull
    private CategoryDto category;
    private long confirmedRequests;
    @NotNull
    private LocalDateTime eventDate;
    @NotNull
    private UserShortDto initiator;
    private Boolean paid;
    @NotBlank
    private String title;
    private long views;
}
