package ru.practicum.ewm.pub;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.common.dto.CategoryDto;
import ru.practicum.ewm.common.param.PaginationRequest;
import ru.practicum.ewm.common.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class PublicCategoryController {
    private final CategoryService service;

    @GetMapping
    List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") int from,
                                    @RequestParam(defaultValue = "10") int size) {
        return service.getCategories(new PaginationRequest(size, from));
    }

    @GetMapping("/{catId}")
    CategoryDto getCategory(@PathVariable long catId) {
        return service.getCategory(catId);
    }
}
