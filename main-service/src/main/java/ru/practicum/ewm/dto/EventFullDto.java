package ru.practicum.ewm.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.model.Location;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class EventFullDto extends EventDtoBase {
    @NotNull
    private LocalDateTime createdOn;
    @NotBlank
    private String description;
    private Location location;
    private Integer participantLimit;
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    @NotNull
    private EventState state;
}
