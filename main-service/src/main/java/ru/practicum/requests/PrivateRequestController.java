package ru.practicum.requests;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.requests.dto.ParticipationRequestDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class PrivateRequestController {

    private final RequestService requestService;

    @Autowired
    public PrivateRequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    // Получение информации о заявках текущего пользователя на участие в чужих событиях
    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        log.info(">>>> HTTP GET: Получен запрос на получение заявок пользователя на участие в событиях. "
                 + "User ID = " + userId);
        return requestService.getUserRequests(userId);
    }

    // Добавление запроса от текущего пользователя на участие в событии
    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addUserRequestToEvent(@PathVariable Long userId,
                                                         @RequestParam Long eventId) {
        log.info(">>>> HTTP POST: Получен запрос на добавление заявки пользователя на участие в событии. "
                 + "User ID = " + userId + ", Event ID = " + eventId);
        return requestService.addUserRequestToEvent(userId, eventId);
    }

    // Отмена своего запроса на участие в событии
    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelUserRequestToEvent(@PathVariable Long userId,
                                                            @PathVariable Long requestId) {
        log.info(">>>> HTTP PATCH: Получен запрос на отмену заявки пользователя на участие в событиях. "
                 + "User ID = " + userId + ", Request ID = " + requestId);
        return requestService.cancelUserRequestToEvent(userId, requestId);
    }
}
