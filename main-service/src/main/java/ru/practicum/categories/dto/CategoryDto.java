package ru.practicum.categories.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

// Категория
@Data
@AllArgsConstructor
public class CategoryDto {
    private Long id;            // Идентификатор категории
    @NotBlank
    @Size(min = 1, max = 50)
    private String name;        // Название категории
}
