package ru.practicum.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// Пользователь
@Data
@AllArgsConstructor
public class UserDto {
    private Long id;        // Идентификатор
    private String name;    // Имя пользователя
    private String email;   // Электронная почта
}
