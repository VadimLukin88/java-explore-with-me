package ru.practicum.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

// Пользователь
@Data
@AllArgsConstructor
public class UserDto {
    private Long id;        // Идентификатор
    private String name;    // Имя пользователя
    private String email;   // Электронная почта
}
