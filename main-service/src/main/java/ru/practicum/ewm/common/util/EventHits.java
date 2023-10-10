package ru.practicum.ewm.common.util;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventHits {
    private Long eventId;
    private Long hits;
}
