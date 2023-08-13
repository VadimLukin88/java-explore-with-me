package ru.practicum.requests.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// Заявка на участие в событии
@Data
@Builder
public class ParticipationRequestDto {
    private Long id;            // Идентификатор заявки
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;     // Дата и время создания заявки
    private Long event;         // Идентификатор события
    private Long requester;     // Идентификатор пользователя, отправившего заявку
    private RequestStatus status;      // Какие значения должны быть тут ??? example: PENDING
}
