package ru.practicum.compilations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.events.dto.EventShortDto;

import java.util.List;

//Подборка событий
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompilationDto {
    private Long id;                    // Идентификатор
    private String title;               // Заголовок подборки
    private Boolean pinned;             // Закреплена ли подборка на главной странице сайта
    private List<EventShortDto> events; // Список событий входящих в подборку
}
