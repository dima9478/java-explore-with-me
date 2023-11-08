package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.NewPoiRequest;
import ru.practicum.ewm.dto.PoiDto;
import ru.practicum.ewm.dto.PoiShortDto;
import ru.practicum.ewm.model.Poi;
import ru.practicum.ewm.model.PoiState;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class PoiMapper {
    public static Poi toPoi(NewPoiRequest dto) {
        return Poi.builder()
                .impactRadius(dto.getImpactRadius())
                .location(dto.getLocation())
                .name(dto.getName())
                .state(PoiState.PENDING)
                .build();
    }

    public static PoiDto toDto(Poi poi) {
        return PoiDto.builder()
                .id(poi.getId())
                .state(poi.getState())
                .location(poi.getLocation())
                .impactRadius(poi.getImpactRadius())
                .name(poi.getName())
                .build();
    }

    public static PoiShortDto toShortDto(Poi poi) {
        return PoiShortDto.builder()
                .id(poi.getId())
                .location(poi.getLocation())
                .name(poi.getName())
                .impactRadius(poi.getImpactRadius())
                .build();
    }

    public static List<PoiDto> toPoiDtoList(List<Poi> pois) {
        return pois.stream()
                .map(PoiMapper::toDto)
                .collect(Collectors.toList());
    }

    public static List<PoiShortDto> toPoiShortDtoList(List<Poi> pois) {
        return pois.stream()
                .map(PoiMapper::toShortDto)
                .collect(Collectors.toList());
    }
}
