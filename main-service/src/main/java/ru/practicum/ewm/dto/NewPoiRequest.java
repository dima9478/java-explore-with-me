package ru.practicum.ewm.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.model.Location;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Data
@Builder
public class NewPoiRequest {
    @NotBlank
    @Size(min = 3, max = 120)
    private String name;
    @NotNull
    private Location location;
    @Positive
    private double impactRadius;
}
