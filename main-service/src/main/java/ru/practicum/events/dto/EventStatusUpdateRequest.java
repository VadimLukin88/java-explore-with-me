package ru.practicum.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO для запроса массового апдейта статуса событий администраторами (batch update)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventStatusUpdateRequest {
    private Long eventId;                   // Id события
    private AdminStateAction stateAction;   // // Новое состояние события. Enum
    private String adminComment;            // Комментарий администратора
}
