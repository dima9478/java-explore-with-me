package ru.practicum.ewm.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.PoiState;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class PoiDto {
    private Long id;
    @NotBlank
    private String name;
    @NotNull
    private Location location;
    private double impactRadius;
    @NotNull
    private PoiState state;
}
