package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.model.PoiState;
import ru.practicum.ewm.param.Bbox;
import ru.practicum.ewm.param.PaginationRequest;

import javax.validation.Valid;
import java.util.EnumSet;
import java.util.List;

public interface PoiService {
    PoiDto addPoiByAdmin(@Valid NewPoiRequest dto);

    PoiDto changePoi(long poiId, @Valid UpdatePoiRequest dto);

    PoiDto addPoiByUser(@Valid NewPoiRequest dto);

    List<PoiShortDto> getPois(String text, Bbox bbox, @Valid PaginationRequest req);

    List<PoiDto> getPoisByAdmin(Bbox bbox, EnumSet<PoiState> states, @Valid PaginationRequest req);

    List<EventShortDto> getPoiEvents(long poiId);
}
