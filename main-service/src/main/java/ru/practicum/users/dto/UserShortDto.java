package ru.practicum.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// Пользователь (краткая информация)
@Data
@AllArgsConstructor
public class UserShortDto {
    private Long id;        // Идентификатор
    private String name;    // Имя пользователя
}
