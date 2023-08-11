package ru.practicum.categories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/admin/categories")
@Validated
public class AdminCategoryController {

    private final CategoriesService categoriesService;

    @Autowired
    public AdminCategoryController(CategoriesService categoriesService) {
        this.categoriesService = categoriesService;
    }

    // Добавление новой категории
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@Valid @RequestBody NewCategoryDto newCatDto) {
        log.info(">>>> HTTP_POST: Получен запрос на добавление новой категории. New DTO = {}", newCatDto);
        return categoriesService.addCategory(newCatDto);
    }

    // Удаление категории
    @DeleteMapping("/{catId}")
    public void deleteCategory(@PathVariable Long catId) {
        log.info(">>>> HTTP_DELETE: Получен запрос на удаление категории. Category ID = {}", catId);
        categoriesService.deleteCategory(catId);
    }

    // Изменение категории
    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId,
                                      @Valid @RequestBody CategoryDto updCatDto) {
        log.info(">>>> HTTP_PATCH: Получен запрос на изменение категории. Category ID = {}, Upd DTO = {}", catId, updCatDto);
        return categoriesService.updateCategory(catId, updCatDto);
    }
}
