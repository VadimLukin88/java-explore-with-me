package ru.practicum.categories;

import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;

import java.util.List;

public interface CategoriesService {

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategoryById(Long catId);

    CategoryDto addCategory(NewCategoryDto newCatDto);

    void deleteCategory(Long catId);

    CategoryDto updateCategory(Long catId, CategoryDto updCatDto);
}
