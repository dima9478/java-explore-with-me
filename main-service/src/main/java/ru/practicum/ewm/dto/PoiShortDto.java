package ru.practicum.ewm.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.model.Location;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class PoiShortDto {
    private Long id;
    @NotBlank
    private String name;
    @NotNull
    private Location location;
    private double impactRadius;
}
