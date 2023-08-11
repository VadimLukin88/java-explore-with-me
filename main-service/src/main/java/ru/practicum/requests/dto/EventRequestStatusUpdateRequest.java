package ru.practicum.requests.dto;

import lombok.Data;

import java.util.List;

// Изменение статуса запроса на участие в событии текущего пользователя
@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;  // Идентификаторы запросов на участие в событии текущего пользователя
    private String status;          // Новый статус запроса на участие в событии текущего пользователя
}
