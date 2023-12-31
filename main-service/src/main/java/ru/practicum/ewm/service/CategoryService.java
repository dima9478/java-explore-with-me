package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;
import ru.practicum.ewm.param.PaginationRequest;

import javax.validation.Valid;
import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(@Valid NewCategoryDto dto);

    CategoryDto changeCategory(long catId, @Valid CategoryDto dto);

    void deleteCategory(long catId);

    List<CategoryDto> getCategories(@Valid PaginationRequest pagination);

    CategoryDto getCategory(long categoryId);
}
