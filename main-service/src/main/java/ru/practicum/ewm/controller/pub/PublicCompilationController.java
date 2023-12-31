package ru.practicum.ewm.controller.pub;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.param.PaginationRequest;
import ru.practicum.ewm.service.CompilationService;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class PublicCompilationController {
    private final CompilationService service;

    @GetMapping
    List<CompilationDto> getCompilations(@RequestParam(defaultValue = "false") boolean pinned,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size) {
        return service.getCompilations(pinned, new PaginationRequest(size, from));
    }

    @GetMapping("/{compId}")
    CompilationDto getCompilation(@PathVariable long compId) {
        return service.getCompilation(compId);
    }
}
