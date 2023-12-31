package ru.practicum.ewm.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;
import ru.practicum.ewm.service.CategoryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    private final CategoryService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CategoryDto postCategory(@RequestBody NewCategoryDto dto) {
        return service.addCategory(dto);
    }

    @PatchMapping("/{catId}")
    CategoryDto patchCategory(@PathVariable long catId, @RequestBody CategoryDto dto) {
        return service.changeCategory(catId, dto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteCategory(@PathVariable long catId) {
        service.deleteCategory(catId);
    }
}
