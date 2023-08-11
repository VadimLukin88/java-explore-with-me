package ru.practicum.users.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

// Данные нового пользователя
@Data
public class NewUserRequest {
    @NotBlank
    @Size(min = 2, max = 250)
    private String name;        // имя пользователя
    @NotBlank
    @Email
    @Size(min = 6, max = 254)
    private String email;       // электронная почта
}
