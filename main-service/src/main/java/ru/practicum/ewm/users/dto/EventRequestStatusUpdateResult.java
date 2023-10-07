package ru.practicum.ewm.users.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.common.dto.ParticipationRequestDto;

import java.util.List;

@Data
@Builder
public class EventRequestStatusUpdateResult {
    private List<ParticipationRequestDto> confirmedRequests;
    private List<ParticipationRequestDto> rejectedRequests;
}
