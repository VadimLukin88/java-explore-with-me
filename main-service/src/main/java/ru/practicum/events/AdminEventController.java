package ru.practicum.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.UpdateEventAdminRequest;
import ru.practicum.events.models.EventState;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/events")
@Validated
public class AdminEventController {

    private final EventsService eventsService;

    @Autowired
    public AdminEventController(EventsService eventsService) {
        this.eventsService = eventsService;
    }

    // Поиск событий. Назвать метод в сервисе одинаково, но с разным набором параметров
    @GetMapping
    public List<EventFullDto> getEventsByAdmin(@RequestParam(required = false) Long[] users,
                                               @RequestParam(required = false) EventState[] states,
                                               @RequestParam(required = false) Long[] categories,
                                               //@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                               @RequestParam(required = false) LocalDateTime rangeStart,
                                               //@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                               @RequestParam(required = false) LocalDateTime rangeEnd,
                                               @RequestParam(defaultValue = "0") int from,
                                               @RequestParam(defaultValue = "10") int size) {
        log.info(">>>> HTTP_GET: Получен запрос на получение событий. Параметры запроса: "
                 + "users = {}, states = {}, categories = {}, rangeStart = {}, rangeEnd = {}, from = {}, size = {}",
                users, states, categories, rangeStart, rangeEnd, from, size);

        return eventsService.getEventsByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    // Редактирование данных события и его статуса (отклонение/публикация).
    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        log.info(">>>> HTTP_PATCH: Получен запрос на обновление данных события. Upd EventDTO = {}", updateEventAdminRequest);
        return eventsService.updateEvent(eventId, updateEventAdminRequest);
    }
}
