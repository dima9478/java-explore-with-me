package ru.practicum.ewm.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.NewPoiRequest;
import ru.practicum.ewm.dto.PoiDto;
import ru.practicum.ewm.dto.UpdatePoiRequest;
import ru.practicum.ewm.error.BadRequestException;
import ru.practicum.ewm.model.PoiState;
import ru.practicum.ewm.param.Bbox;
import ru.practicum.ewm.param.PaginationRequest;
import ru.practicum.ewm.service.PoiService;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/poi")
public class AdminPoiController {
    private final PoiService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PoiDto postPoi(@RequestBody NewPoiRequest dto) {
        return service.addPoiByAdmin(dto);
    }

    @PatchMapping("/{poiId}")
    PoiDto patchPoi(@PathVariable long poiId, @RequestBody UpdatePoiRequest dto) {
        return service.changePoi(poiId, dto);
    }

    @GetMapping
    List<PoiDto> getPois(@RequestParam(defaultValue = "-180,-90,180,90") String bbox,
                         @RequestParam(required = false) EnumSet<PoiState> states,
                         @RequestParam(defaultValue = "0") int from,
                         @RequestParam(defaultValue = "10") int size) {
        double[] directions = Arrays.stream(bbox.split(","))
                .mapToDouble(Double::parseDouble)
                .toArray();
        if (directions.length != 4) {
            throw new BadRequestException(
                    "Incorrect parameter bbox",
                    "Bbox should contain 4 double values through commas");
        }
        return service.getPoisByAdmin(
                new Bbox(directions[0], directions[1], directions[2], directions[3]),
                states,
                new PaginationRequest(size, from));
    }
}
