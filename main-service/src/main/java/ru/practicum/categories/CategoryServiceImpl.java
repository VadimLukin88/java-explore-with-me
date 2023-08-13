package ru.practicum.categories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;
import ru.practicum.categories.models.Category;
import ru.practicum.events.EventRepository;
import ru.practicum.exceptions.DataNotFoundException;
import ru.practicum.exceptions.ValidationException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoriesService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, EventRepository eventRepository) {
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
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
        if (categoryRepository.findByName(newCatDto.getName()).isPresent()) {
            throw new ValidationException("Category name already exist", HttpStatus.CONFLICT);
        }
        Category category = CategoryMapper.mapDtoToCategory(newCatDto);

        return CategoryMapper.mapCategoryToDto(categoryRepository.save(category));
    }

    // Удаление категории (для Admin контроллера)
    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        Category category = categoryRepository.findById(catId)
                                               .orElseThrow(() -> new DataNotFoundException("Category not found", HttpStatus.NOT_FOUND));

        if (eventRepository.existsByCategory_Id(catId)) {
            throw new ValidationException("Category used in events", HttpStatus.CONFLICT);
        }
        categoryRepository.deleteById(catId);
    }

    // Изменение категории (для Admin контроллера)
    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto updCatDto) {
        Category category = categoryRepository.findById(catId)
                                              .orElseThrow(() -> new DataNotFoundException("Category not found", HttpStatus.NOT_FOUND));

        Optional<Category> existCat = categoryRepository.findByName(updCatDto.getName());
        if (existCat.isPresent() && !existCat.get().getId().equals(catId)) {
            throw new ValidationException("Category name already exist", HttpStatus.CONFLICT);
        }
        category.setName(updCatDto.getName());
        return CategoryMapper.mapCategoryToDto(category);
    }

}
