package ru.practicum.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.events.models.Location;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

// Данные для изменения информации о событии. Если поле в запросе не указано (равно null) - значит изменение этих данных не треубется.
@Data
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000)
    private String annotation;      // Новая аннотация
    private Long category;          // Новая категория
    @Size(min = 20, max = 7000)
    private String description;     // Новое описание
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;       // Новые дата и время на которые намечено событие. Дата и время указываются в формате "yyyy-MM-dd HH:mm:ss"
    private Location location;      // Широта и долгота места проведения события
    private Boolean paid;           // Новое значение флага о платности мероприятия
    private Integer participantLimit;   // Новый лимит пользователей
    private Boolean requestModeration;  // Нужна ли пре-модерация заявок на участие
    private AdminStateAction stateAction;     // Новое состояние события. Enum
    @Size(min = 3, max = 120)
    private String title;           // Новый заголовок
}
