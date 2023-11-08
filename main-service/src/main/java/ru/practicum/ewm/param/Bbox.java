package ru.practicum.ewm.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Bbox { // bounding box
    private double west;
    private double south;
    private double east;
    private double north;
}
