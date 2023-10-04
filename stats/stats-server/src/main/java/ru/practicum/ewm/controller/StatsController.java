package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.ewm.param.StatParams;
import ru.practicum.ewm.service.HitService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final HitService service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    void addHit(@RequestBody EndpointHitDto hitDto) {
        service.addHit(hitDto);
    }

    @GetMapping("/stats")
    List<ViewStats> getStats(@RequestParam
                             @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                             @RequestParam
                             @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                             @RequestParam(required = false) List<String> uris,
                             @RequestParam(defaultValue = "false") boolean unique
                             ) {
        return service.getUriHits(new StatParams(start, end, uris, unique));
    }
}
