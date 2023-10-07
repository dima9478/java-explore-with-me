package ru.practicum.ewm.users.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.common.dto.ParticipationRequestDto;
import ru.practicum.ewm.common.model.Request;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class RequestMapper {
    public ParticipationRequestDto toDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .event(request.getEvent().getId())
                .created(request.getCreated())
                .requester(request.getRequestor().getId())
                .status(request.getStatus().name())
                .build();
    }

    public List<ParticipationRequestDto> toDtoList(List<Request> requests) {
        return requests.stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }
}
