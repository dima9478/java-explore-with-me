package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.ewm.model.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class HitMapper {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EndpointHit toHit(EndpointHitDto dto) {
        return EndpointHit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(LocalDateTime.parse(dto.getTimestamp(), formatter))
                .build();
    }
}
