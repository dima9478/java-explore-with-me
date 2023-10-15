package ru.practicum.ewm.controller.pub;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.PoiShortDto;
import ru.practicum.ewm.error.BadRequestException;
import ru.practicum.ewm.param.Bbox;
import ru.practicum.ewm.param.PaginationRequest;
import ru.practicum.ewm.service.PoiService;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/poi")
public class PublicPoiController {
    private final PoiService service;

    @GetMapping
    List<PoiShortDto> getPois(@RequestParam(required = false) String text,
                              @RequestParam(defaultValue = "-180,-90,180,90") String bbox,
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
        return service.getPois(
                text,
                new Bbox(directions[0], directions[1], directions[2], directions[3]),
                new PaginationRequest(size, from));
    }

    @GetMapping("/{poiId}/events")
    List<EventShortDto> getPoiEvents(@PathVariable long poiId) {
        return service.getPoiEvents(poiId);
    }
}
