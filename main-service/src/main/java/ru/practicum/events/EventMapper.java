package ru.practicum.events;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.categories.CategoryMapper;
import ru.practicum.events.dto.*;
import ru.practicum.events.models.Event;
import ru.practicum.users.UserMapper;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {

    public static Event mapDtoToEvent(NewEventDto newEventDto) {
        return Event.builder()
            .id(null)
            .title(newEventDto.getTitle())
            .annotation(newEventDto.getAnnotation())
            .description(newEventDto.getDescription())
            .eventDate(newEventDto.getEventDate())
            .location(newEventDto.getLocation())
            .paid(newEventDto.getPaid())
            .participantLimit(newEventDto.getParticipantLimit())
            .requestModeration(newEventDto.getRequestModeration())
            .title(newEventDto.getTitle())
            .build();
    }

    public static Event mapDtoToEvent(UpdateEventAdminRequest updEventDto) {
        return Event.builder()
            .id(null)
            .title(updEventDto.getTitle())
            .annotation(updEventDto.getAnnotation())
            .description(updEventDto.getDescription())
            .eventDate(updEventDto.getEventDate())
            .location(updEventDto.getLocation())
            .paid(updEventDto.getPaid())
            .participantLimit(updEventDto.getParticipantLimit())
            .requestModeration(updEventDto.getRequestModeration())
            .title(updEventDto.getTitle())
            .build();
    }

    public static Event mapDtoToEvent(UpdateEventUserRequest updEventDto) {
        return Event.builder()
            .id(null)
            .title(updEventDto.getTitle())
            .annotation(updEventDto.getAnnotation())
            .description(updEventDto.getDescription())
            .eventDate(updEventDto.getEventDate())
            .location(updEventDto.getLocation())
            .paid(updEventDto.getPaid())
            .participantLimit(updEventDto.getParticipantLimit())
            .requestModeration(updEventDto.getRequestModeration())
            .title(updEventDto.getTitle())
            .build();
    }

    public static EventFullDto mapEventToDto(Event event) {
        return EventFullDto.builder()
            .id(event.getId())
            .annotation(event.getAnnotation())
            .category(CategoryMapper.mapCategoryToDto(event.getCategory()))
            .createdOn(event.getCreatedOn())
            .description(event.getDescription())
            .eventDate(event.getEventDate())
            .initiator(UserMapper.mapUserToShortDto(event.getInitiator()))
            .location(event.getLocation())
            .paid(event.getPaid())
            .participantLimit(event.getParticipantLimit())
            .publishedOn(event.getPublishedOn())
            .requestModeration(event.getRequestModeration())
            .state(event.getState().name())
            .title(event.getTitle())
            .confirmedRequests(0L)
            .views(0L)
            .build();
    }

    public static EventShortDto mapEventToShortDto(Event event) {
        return EventShortDto.builder()
            .id(event.getId())
            .annotation(event.getAnnotation())
            .category(CategoryMapper.mapCategoryToDto(event.getCategory()))
            .confirmedRequests(0L)
            .eventDate(event.getEventDate())
            .initiator(UserMapper.mapUserToShortDto(event.getInitiator()))
            .paid(event.getPaid())
            .title(event.getTitle())
            .views(0L)
            .build();
    }
}
