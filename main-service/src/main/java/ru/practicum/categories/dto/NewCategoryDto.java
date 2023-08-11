package ru.practicum.categories.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

// Данные для добавления новой категории
@Data
public class NewCategoryDto {
    @NotBlank
    @Size(min = 1, max = 50)
    private String name;        // Название категории
}
