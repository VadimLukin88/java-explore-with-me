package ru.practicum.events;

import ru.practicum.events.dto.*;
import ru.practicum.events.models.EventState;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventsService {

    List<EventShortDto>  findEvents(String text,
                                    Long[] categories,
                                    Boolean paid,
                                    LocalDateTime rangeStart,
                                    LocalDateTime rangeEnd,
                                    Boolean onlyAvailable,
                                    EventSort sort,
                                    int from,
                                    int size,
                                    HttpServletRequest request);

    EventFullDto getEventById(Long id, HttpServletRequest request);

    List<EventShortDto> getUserEvent(Long userId, int from, int size);

    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getEventById(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<EventFullDto> getEventsByAdmin(Long[] users,
                                        EventState[] states,
                                        Long[] categories,
                                        LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd,
                                        int from,
                                        int size);

    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    EventStatusUpdateResult batchUpdateEventStatus(List<EventStatusUpdateRequest> eventDtoList);
}
