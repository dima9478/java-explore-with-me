package ru.practicum.ewm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.model.Location;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewEventDto {
    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;
    @Positive
    private long category;
    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;
    @NotNull
    private LocalDateTime eventDate;
    @NotNull
    private Location location;
    private Boolean paid = false;
    @PositiveOrZero
    private int participantLimit = 0;
    private Boolean requestModeration = true;
    @NotBlank
    @Size(min = 3, max = 120)
    private String title;
}
