package ru.practicum.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.requests.RequestService;
import ru.practicum.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.dto.UpdateEventUserRequest;

import javax.validation.Valid;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@Validated
public class PrivateEventController {

    private final EventsService eventsService;
    private final RequestService requestService;

    @Autowired
    public PrivateEventController(EventsService eventsService, RequestService requestService) {
        this.eventsService = eventsService;
        this.requestService = requestService;
    }

    // Получение событий, добавленных текущим пользователем
    @GetMapping("/{userId}/events")
    public List<EventShortDto> getUserEvents(@PathVariable Long userId,
                                             @PositiveOrZero
                                             @RequestParam(defaultValue = "0") int from,
                                             @PositiveOrZero
                                             @RequestParam(defaultValue = "10") int size) {
        log.info(">>>> HTTP_GET: Получение событий, добавленных текущим пользователем. Параметры: "
                 + " User ID = {}, from = {}, size = {}", userId, from, size);
        return eventsService.getUserEvent(userId, from, size);
    }

    // Добавление нового события
    @PostMapping("/{userId}/events")
    public EventFullDto addEvent(@PathVariable Long userId,
                                 @Valid @RequestBody NewEventDto newEventDto) {
        log.info(">>>> HTTP_POST: Добавление нового события. New EventDTO = {}", newEventDto);
        return eventsService.addEvent(userId, newEventDto);
    }

    // Получение полной информации о событии добавленном текущим пользователем
    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getEventById(@PathVariable Long userId,
                                     @PathVariable Long eventId) {
        log.info(">>>> HTTP_GET: Получение пользователем полной информации о событии. User ID = {}, Event ID = {}", userId, eventId);
        return eventsService.getEventById(userId, eventId);
    }

    // Изменение события добавленного текущим пользователем
    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        log.info(">>>> HTTP_PATCH: Изменение события пользователем. Upd EventDTO = {}", updateEventUserRequest);
        return eventsService.updateEvent(userId, eventId, updateEventUserRequest);
    }

    // Получение информации о запросах на участие в событии текущего пользователя
    @GetMapping("/{userId}/events/{eventId}/requests")
    public ParticipationRequestDto getRequestForEvent(@PathVariable Long userId,
                                                      @PathVariable Long eventId) {
        log.info(">>>> HTTP_GET: Получение пользователем запросов на участие в событии. User ID = {}, Event ID = {}", userId, eventId);
        return requestService.getRequestForEvent(userId, eventId);
    }

    // Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя
    @PatchMapping("/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable Long userId,
                                                              @PathVariable Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest statusUpdRequest) {
        log.info(">>>> HTTP_PATCH: Подтверждение/отмена заявок на участие в событии. "
                 + "User Id = {}, Event ID = {},  RequestStatusUpdateDTO = {}", userId, eventId, statusUpdRequest);
        return requestService.updateRequestStatus(userId, eventId, statusUpdRequest);
    }

}
