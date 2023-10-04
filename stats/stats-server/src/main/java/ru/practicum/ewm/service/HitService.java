package ru.practicum.ewm.service;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.ewm.param.StatParams;

import java.util.List;

public interface HitService {
    void addHit(EndpointHitDto hitDto);

    List<ViewStats> getUriHits(StatParams params);
}
