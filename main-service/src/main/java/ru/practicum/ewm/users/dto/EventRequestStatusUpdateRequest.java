package ru.practicum.ewm.users.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    @NotNull
    @Size(min = 1)
    private List<Long> requestIds;
    private UpdateRequestStatus status;
}
