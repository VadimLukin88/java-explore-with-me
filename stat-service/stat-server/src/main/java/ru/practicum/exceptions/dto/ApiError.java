package ru.practicum.exceptions.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

// Сведения об ошибке
@Data
@Builder
public class ApiError {
    private List<String> errors;
    private String message;
    private String reason;
    private String status; // Код статуса HTTP-ответа. enum
    private String timestamp;
}
