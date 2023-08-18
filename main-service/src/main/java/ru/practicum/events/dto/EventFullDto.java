package ru.practicum.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.events.models.Location;
import ru.practicum.users.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventFullDto {
    private Long id;            // Идентификатор
    private String annotation;  // Краткое описание
    private CategoryDto category;   // Категория
    private Long confirmedRequests; // Количество одобренных заявок на участие в данном событии
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;       // Дата и время создания события (в формате "yyyy-MM-dd HH:mm:ss")
    private String description;     // Полное описание события
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;       // Дата и время на которые намечено событие (в формате "yyyy-MM-dd HH:mm:ss")
    private UserShortDto initiator; // Пользователь (краткая информация)
    private Location location;      // Широта и долгота места проведения события
    private Boolean paid;           // Нужно ли оплачивать участие
    private Integer participantLimit;   // Default = 0. Ограничение на количество участников. Значение 0 - означает отсутствие ограничения
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;     // Дата и время публикации события (в формате "yyyy-MM-dd HH:mm:ss")
    private Boolean requestModeration; // Нужна ли пре-модерация заявок на участие. Default = true
    private String state;           // Список состояний жизненного цикла события. enum
    private String title;           // Заголовок
    private Long views;             // Количество просмотрев события
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String adminComment;    // Комментарий админа. Будем ставить его с помощью сеттера, только когда отдаём Dto
                                    // владельцу события или админу.
}
