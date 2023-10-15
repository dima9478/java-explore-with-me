package ru.practicum.ewm.controller.priv;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.NewPoiRequest;
import ru.practicum.ewm.dto.PoiDto;
import ru.practicum.ewm.service.PoiService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/poi")
public class UsersPoiController {
    private final PoiService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PoiDto postPoi(@RequestBody NewPoiRequest dto) {
        return service.addPoiByUser(dto);
    }
}
