package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.NewCompilationDto;
import ru.practicum.ewm.dto.UpdateCompilationRequest;
import ru.practicum.ewm.param.PaginationRequest;

import javax.validation.Valid;
import java.util.List;

public interface CompilationService {
    CompilationDto addCompilation(@Valid NewCompilationDto dto);

    CompilationDto changeCompilation(long compId, @Valid UpdateCompilationRequest dto);

    void deleteCompilation(long compId);

    List<CompilationDto> getCompilations(boolean pinned, @Valid PaginationRequest paginationRequest);

    CompilationDto getCompilation(long compId);
}
