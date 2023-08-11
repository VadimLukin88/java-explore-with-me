package ru.practicum.categories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;
import ru.practicum.categories.models.Category;
import ru.practicum.exceptions.DataNotFoundException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoriesService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Получение категорий (для Public контроллера)
    // В случае, если по заданным фильтрам не найдено ни одной категории, возвращает пустой список
    @Override
    @Transactional
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from, size);

        return categoryRepository.findAll(pageable).stream()
            .map(CategoryMapper::mapCategoryToDto)
            .collect(Collectors.toList());
    }

    // получение категории по Id (для Public контроллера)
    @Override
    @Transactional
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId)
                                              .orElseThrow(() -> new DataNotFoundException("Category not found", HttpStatus.NOT_FOUND));

        return CategoryMapper.mapCategoryToDto(category);
    }

    // Добавление новой категории (для Admin контроллера)
    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCatDto) {
        Category category = CategoryMapper.mapDtoToCategory(newCatDto);
        return CategoryMapper.mapCategoryToDto(categoryRepository.save(category));
    }

    // Удаление категории (для Admin контроллера)
    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        Category category = categoryRepository.findById(catId)
                                              .orElseThrow(() -> new DataNotFoundException("Category not found", HttpStatus.NOT_FOUND));

        categoryRepository.delete(category);
    }

    // Изменение категории (для Admin контроллера)
    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto updCatDto) {
        Category category = categoryRepository.findById(catId)
                                              .orElseThrow(() -> new DataNotFoundException("Category not found", HttpStatus.NOT_FOUND));

        category.setName(updCatDto.getName());
        return CategoryMapper.mapCategoryToDto(categoryRepository.save(category));
    }
}
