package ru.practicum.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;
import ru.practicum.events.models.Location;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

//Новое событие
@Data
public class NewEventDto {
    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;      // Краткое описание
    private Long category;          // Id категории
    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;     // Категория
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;       // Дата и время на которые намечено событие
    @NotNull
    private Location location;      // Широта и долгота места проведения события
    @JsonSetter(nulls = Nulls.SKIP)
    private Boolean paid = false;           // Нужно ли оплачивать участие
    @JsonSetter(nulls = Nulls.SKIP)
    private int participantLimit = 0; // Ограничение на количество участников. Значение 0 - означает отсутствие ограничения. Default = 0
    @JsonSetter(nulls = Nulls.SKIP)
    private Boolean requestModeration = true; // Нужна ли пре-модерация заявок на участие. Default = true
    @NotBlank
    @Size(min = 3, max = 120)
    private String title;           // Заголовок
}
