package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.ewm.error.BadRequestException;
import ru.practicum.ewm.mapper.HitMapper;
import ru.practicum.ewm.param.StatParams;
import ru.practicum.ewm.repository.EndpointHitRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {
    private final EndpointHitRepository repository;

    @Override
    public void addHit(EndpointHitDto hitDto) {
        repository.save(HitMapper.toHit(hitDto));
    }

    @Override
    public List<ViewStats> getUriHits(StatParams params) {
        if (!params.getEnd().isAfter(params.getStart())) {
            throw new BadRequestException("End must be after start");
        }
        if (params.getUris() != null) {
            if (params.isUnique()) {
                return repository.getIpUniqueStats(params.getStart(), params.getEnd(), params.getUris());
            }
            return repository.getStats(params.getStart(), params.getEnd(), params.getUris());
        } else {
            if (params.isUnique()) {
                return repository.getIpUniqueStats(params.getStart(), params.getEnd());
            }
            return repository.getStats(params.getStart(), params.getEnd());
        }
    }
}
