package ru.practicum.ewm.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.admin.dto.CompilationDto;
import ru.practicum.ewm.admin.dto.NewCompilationDto;
import ru.practicum.ewm.admin.dto.UpdateCompilationRequest;
import ru.practicum.ewm.common.service.CompilationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
public class AdminCompilationController {
    private final CompilationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CompilationDto postCompilation(@RequestBody NewCompilationDto dto) {
        return service.addCompilation(dto);
    }

    @PatchMapping("/{compId}")
    CompilationDto patchCompilation(@PathVariable long compId, @RequestBody UpdateCompilationRequest dto) {
        return service.changeCompilation(compId, dto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteCompilation(@PathVariable long compId) {
        service.deleteCompilation(compId);
    }
}
