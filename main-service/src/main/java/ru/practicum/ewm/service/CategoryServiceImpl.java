package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;
import ru.practicum.ewm.error.ConflictException;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.param.PaginationRequest;
import ru.practicum.ewm.param.PaginationRequestConverter;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.util.DbAvailabilityChecker;

import javax.validation.Valid;
import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final DbAvailabilityChecker availabilityChecker;

    @Override
    public CategoryDto addCategory(@Valid NewCategoryDto dto) {
        Category category;
        try {
            category = categoryRepository.save(CategoryMapper.toCategory(dto));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Integrity constraint has been violated.", e.getMessage());
        }

        return CategoryMapper.toDto(category);
    }

    @Override
    public CategoryDto changeCategory(long catId, @Valid CategoryDto dto) {
        Category category = CategoryMapper.toCategory(dto);
        category.setId(catId);

        availabilityChecker.checkCategory(catId);
        try {
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Integrity constraint has been violated.", e.getMessage());
        }

        return CategoryMapper.toDto(category);
    }

    @Transactional
    @Override
    public void deleteCategory(long catId) {
        availabilityChecker.checkCategory(catId);
        if (!eventRepository.findAllByCategoryId(catId).isEmpty()) {
            throw new ConflictException(
                    "For the requested operation the conditions are not met.",
                    "The category is not empty"
            );
        }

        categoryRepository.deleteById(catId);
    }

    @Override
    public List<CategoryDto> getCategories(PaginationRequest pagination) {
        List<Category> categories = categoryRepository.findAll(
                PaginationRequestConverter.toPageable(pagination, Sort.by(Sort.Direction.ASC, "id"))
        ).getContent();

        return CategoryMapper.toDtoList(categories);
    }

    @Override
    public CategoryDto getCategory(long categoryId) {
        Category category = availabilityChecker.checkCategory(categoryId);

        return CategoryMapper.toDto(category);
    }
}
