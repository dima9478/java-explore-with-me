package ru.practicum.ewm.common.param;

import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.common.model.EventState;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

@Data
@Builder
public class ExtendedEventFilter {
    private List<Long> users;
    private EnumSet<EventState> states;
    private List<Long> categories;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
}
