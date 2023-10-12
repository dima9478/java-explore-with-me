package ru.practicum.ewm.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventConfirmedRequestCount {
    private Long eventId;
    private Long confirmedRequests;
}
