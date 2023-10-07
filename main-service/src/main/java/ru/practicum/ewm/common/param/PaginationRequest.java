package ru.practicum.ewm.common.param;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Min;

@AllArgsConstructor
@Data
public class PaginationRequest {
    @Min(1)
    private int size;
    @Min(0)
    private int from;
}
