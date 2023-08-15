package ru.practicum.categories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.categories.dto.CategoryDto;

import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/categories")
@Validated
public class PublicCategoryController {

    private final CategoriesService categoriesService;

    @Autowired
    public PublicCategoryController(CategoriesService categoriesService) {
        this.categoriesService = categoriesService;
    }

    // Получение списка всех категорий
    @GetMapping
    public List<CategoryDto> getCategories(@PositiveOrZero
                                           @RequestParam(defaultValue = "0") int from,
                                           @PositiveOrZero
                                           @RequestParam(defaultValue = "10") int size) {
        log.info(">>>> HTTP_GET: Получен запрос на получение всех категорий. From = {}, Size = {}", from, size);
        return categoriesService.getCategories(from, size);
    }

    // Получение информации о категории по её Id
    @GetMapping("/{catId}")
    public CategoryDto getCategoryById(@PathVariable Long catId) {
        log.info(">>>> HTTP_GET: Получен запрос на получение категории по ID. Category ID = {}", catId);
        return categoriesService.getCategoryById(catId);
    }
}
