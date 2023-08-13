package ru.practicum.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.EventSort;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/events")
public class PublicEventController {

    private final EventsService eventsService;

    @Autowired
    public PublicEventController(EventsService eventsService) {
        this.eventsService = eventsService;
    }

    @GetMapping
    public List<EventShortDto> findEvents(@RequestParam(required = false) String text,
                                          @RequestParam(required = false) Long[] categories,
                                          @RequestParam(required = false) Boolean paid,
                                          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                          @RequestParam(required = false) LocalDateTime rangeStart,
                                          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                          @RequestParam(required = false) LocalDateTime rangeEnd,
                                          @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                          @RequestParam(defaultValue = "EVENT_DATE") EventSort sort,    //enum EVENT_DATE, VIEWS
                                          @RequestParam(defaultValue = "0") int from,
                                          @RequestParam(defaultValue = "10") int size,
                                          HttpServletRequest request) {
        log.info(">>>> HTTP_GET: Поиск событий. Параметры поиска: text = {}, categories = {}, paid = {}, "
                 + "rangeStart = {}, rangeEnd = {}, onlyAvailable = {}, sort = {}, from = {}, size = {}",
                 text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        return eventsService.findEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);
    }

    // Получение подробной информации об опубликованном событии по его идентификатору
    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable Long id, HttpServletRequest request) {
        log.info(">>>> HTTP_GET: Получение события по ID. Event ID = {}", id);
        return eventsService.getEventById(id, request);
    }
}
