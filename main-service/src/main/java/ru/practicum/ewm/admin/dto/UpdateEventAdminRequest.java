package ru.practicum.ewm.admin.dto;

import lombok.Data;
import ru.practicum.ewm.common.model.Location;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000)
    private String annotation;
    private Integer category;
    @Size(min = 20, max = 7000)
    private String description;
    private LocalDateTime eventDate;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private AdminEventStateAction stateAction;
    @Size(min = 3, max = 120)
    private String title;
}
