package ru.practicum.ewm.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventRequestCount {
    private Long eventId;
    private Long count;
}
