package ru.practicum.compilations.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

// Изменение информации о подборке событий. Если поле в запросе не указано (равно null) - значит изменение этих данных не треубется.
@Data
public class UpdateCompilationRequest {
    private List<Long> events;   // Список событий входящих в подборку
    private Boolean pinned;     // Закреплена ли подборка на главной странице сайта
    @NotBlank
    @Size(min = 1, max = 50)
    private String title;       // Заголовок подборки
}
