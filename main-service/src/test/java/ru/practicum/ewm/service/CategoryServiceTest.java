package ru.practicum.ewm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.param.PaginationRequest;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.util.DbAvailabilityChecker;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    DbAvailabilityChecker availabilityChecker;
    @InjectMocks
    CategoryServiceImpl service;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category(1L, "name");
    }

    @Test
    void addCategory() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDto dto = service.addCategory(NewCategoryDto.builder().name("name").build());

        verify(categoryRepository, times(1)).save(any(Category.class));
        assertThat(dto.getId(), equalTo(1L));
        assertThat(dto.getName(), equalTo(category.getName()));
    }

    @Test
    void changeCategory() {
        CategoryDto dto = CategoryDto.builder()
                .name("name2").build();
        Category cat = category.toBuilder().name("name2").build();
        when(categoryRepository.save(cat)).thenReturn(cat);

        CategoryDto dto1 = service.changeCategory(1L, dto);

        verify(availabilityChecker, times(1)).checkCategory(1L);
        verify(categoryRepository, times(1)).save(any(Category.class));
        assertThat(dto1.getName(), equalTo(cat.getName()));
    }

    @Test
    void deleteCategory() {
        when(eventRepository.findAllByCategoryId(1L)).thenReturn(List.of());

        service.deleteCategory(1L);

        verify(availabilityChecker, times(1)).checkCategory(1L);
        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void getCategories() {
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        List<CategoryDto> categories = service.getCategories(new PaginationRequest(10, 0));

        verify(categoryRepository, times(1)).findAll(any(Pageable.class));
        assertThat(categories, empty());
    }

    @Test
    void getCategory() {
        when(availabilityChecker.checkCategory(1L)).thenReturn(category);

        CategoryDto dto = service.getCategory(1L);

        assertThat(dto.getName(), equalTo(category.getName()));
        assertThat(dto.getId(), equalTo(category.getId()));
    }
}
