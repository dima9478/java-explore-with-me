package ru.practicum.ewm.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.error.BadRequestException;
import ru.practicum.ewm.error.ConflictException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.mapper.PoiMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.param.Bbox;
import ru.practicum.ewm.param.PaginationRequest;
import ru.practicum.ewm.param.PaginationRequestConverter;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.PoiRepository;
import ru.practicum.ewm.util.DbAvailabilityChecker;
import ru.practicum.ewm.util.EventDtoAuxiliaryProcessor;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class PoiServiceImpl implements PoiService {
    private final PoiRepository poiRepository;
    private final EventRepository eventRepository;
    private final DbAvailabilityChecker availabilityChecker;
    private final EventDtoAuxiliaryProcessor eventProcessor;

    @Override
    public PoiDto addPoiByAdmin(@Valid NewPoiRequest dto) {
        return addPoi(dto, PoiState.CONFIRMED);
    }

    @Override
    public PoiDto changePoi(long poiId, @Valid UpdatePoiRequest dto) {
        Poi poi = availabilityChecker.checkPoi(poiId);
        validateUpdate(poi, dto);

        applyPatch(poi, dto);
        try {
            poi = poiRepository.save(poi);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Integrity constraint has been violated.", e.getMessage());
        }

        return PoiMapper.toDto(poi);
    }

    @Override
    public PoiDto addPoiByUser(@Valid NewPoiRequest dto) {
        return addPoi(dto, PoiState.PENDING);
    }

    @Override
    public List<PoiShortDto> getPois(String text, Bbox bbox, @Valid PaginationRequest req) {
        validateBbox(bbox);

        QPoi poi = QPoi.poi;
        List<BooleanExpression> conditions = new ArrayList<>();
        conditions.add(makeBboxExpression(bbox));
        conditions.add(poi.state.eq(PoiState.CONFIRMED));
        if (text != null) {
            conditions.add(poi.name.containsIgnoreCase(text));
        }

        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .orElseGet(Expressions.TRUE::isTrue);

        List<Poi> pois = poiRepository.findAll(finalCondition, PaginationRequestConverter.toPageable(req)).toList();

        return PoiMapper.toPoiShortDtoList(pois);
    }

    @Override
    public List<PoiDto> getPoisByAdmin(Bbox bbox, EnumSet<PoiState> states, @Valid PaginationRequest req) {
        validateBbox(bbox);

        QPoi poi = QPoi.poi;
        List<BooleanExpression> conditions = new ArrayList<>();
        conditions.add(makeBboxExpression(bbox));
        if (states != null) {
            conditions.add(poi.state.in(states));
        }
        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .orElseGet(Expressions.TRUE::isTrue);

        List<Poi> pois = poiRepository.findAll(finalCondition, PaginationRequestConverter.toPageable(req)).toList();

        return PoiMapper.toPoiDtoList(pois);
    }

    @Override
    public List<EventShortDto> getPoiEvents(long poiId) {
        Poi poi = availabilityChecker.checkPoi(poiId);
        Location location = poi.getLocation();

        List<Event> events = eventRepository.findByPoiAndState(EventState.PUBLISHED,
                location.getLat(),
                location.getLon(),
                poi.getImpactRadius());
        return EventMapper.toShortDtoList(events,
                eventProcessor.getConfirmedRequestsMap(events),
                eventProcessor.getViewStats(events));
    }

    private PoiDto addPoi(NewPoiRequest dto, PoiState status) {
        Poi poi;
        try {
            poi = PoiMapper.toPoi(dto);
            poi.setState(status);
            poi = poiRepository.save(poi);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Integrity constraint has been violated.", e.getMessage());
        }

        return PoiMapper.toDto(poi);
    }

    private void validateUpdate(Poi poi, UpdatePoiRequest dto) {
        PoiStateAction action = dto.getAction();

        if (action != null && !poi.getState().equals(PoiState.PENDING)) {
            throw new ConflictException(
                    "For the requested operation the conditions are not met.",
                    String.format("Cannot update poi status: %s", poi.getState())
            );
        }
    }

    private Poi applyPatch(Poi poi, UpdatePoiRequest dto) {
        String name = dto.getName();
        Location location = dto.getLocation();
        Double impactRadius = dto.getImpactRadius();
        PoiStateAction action = dto.getAction();

        if (name != null) {
            poi.setName(name);
        }
        if (location != null) {
            poi.setLocation(location);
        }
        if (impactRadius != null) {
            poi.setImpactRadius(impactRadius);
        }
        if (action != null) {
            switch (action) {
                case REJECT_POI:
                    poi.setState(PoiState.REJECTED);
                    break;
                case CONFIRM_POI:
                    poi.setState(PoiState.CONFIRMED);
            }
        }
        return poi;
    }

    private void validateBbox(Bbox bbox) {
        if (bbox.getEast() > 180 || bbox.getWest() < -180 || bbox.getSouth() < -90 || bbox.getNorth() > 90) {
            throw new BadRequestException(
                    "Incorrect bbox values",
                    "West must >= -180, south >= -90, east <= 180, north <= 90"
            );
        }
        if (bbox.getEast() <= bbox.getWest() || bbox.getNorth() <= bbox.getSouth()) {
            throw new BadRequestException(
                    "Incorrect bbox values",
                    "North must have value bigger than south, east - bigger than west"
            );
        }
    }

    private BooleanExpression makeBboxExpression(Bbox bbox) {
        QPoi poi = QPoi.poi;

        return poi.location.lon.goe(bbox.getWest())
                .and(poi.location.lat.goe(bbox.getSouth()))
                .and(poi.location.lon.loe(bbox.getEast()))
                .and(poi.location.lat.loe(bbox.getNorth()));
    }
}
