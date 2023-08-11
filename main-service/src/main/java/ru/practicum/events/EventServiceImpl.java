package ru.practicum.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.categories.CategoryRepository;
import ru.practicum.categories.models.Category;
import ru.practicum.events.dto.*;
import ru.practicum.events.models.Event;
import ru.practicum.events.models.EventState;
import ru.practicum.events.models.Location;
import ru.practicum.exceptions.DataNotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.requests.RequestRepository;
import ru.practicum.requests.dto.RequestStatus;
import ru.practicum.requests.models.ParticipationRequest;
import ru.practicum.users.UserRepository;
import ru.practicum.users.models.User;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventsService{

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository,
                            CategoryRepository categoryRepository, LocationRepository locationRepository,
                            RequestRepository requestRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
        this.requestRepository = requestRepository;
    }

    // Поиск событий (для Public контроллера)
    // Обратите внимание:
    //
    //    это публичный эндпоинт, соответственно в выдаче должны быть только опубликованные события
    //    текстовый поиск (по аннотации и подробному описанию) должен быть без учета регистра букв
    //    если в запросе не указан диапазон дат [rangeStart-rangeEnd], то нужно выгружать события, которые произойдут позже текущей даты и времени
    //    информация о каждом событии должна включать в себя количество просмотров и количество уже одобренных заявок на участие
    //    информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики
    //
    // В случае, если по заданным фильтрам не найдено ни одного события, возвращает пустой список
    @Override
    @Transactional
    public List<EventShortDto> findEvents(String text,
                                          Long[] categories,
                                          Boolean paid,
                                          LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd,
                                          Boolean onlyAvailable,
                                          EventSort sort,
                                          int from,
                                          int size) {
        String searchText;

        if (text != null) {
            searchText = text.toLowerCase();
        } else {
            searchText = text;
        }
        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
            // тут бы конечно прикрутить какую-то константу (в Postgre и Java разные максимальные даты)
            rangeEnd = LocalDateTime.now().plusYears(1000L);
        }
        Pageable pageable;

        if (sort.equals(EventSort.EVENT_DATE)) {
            pageable = PageRequest.of(from, size, Sort.by("eventDate"));
        } else {
            pageable = PageRequest.of(from, size);
        }
        // Ищем события по фильтру
        List<Event> eventList = eventRepository.findEventsForUser(searchText, Arrays.asList(categories), paid, rangeStart, rangeEnd, pageable);
        // Преобразуем из в DTO
        List<EventShortDto> eventDtoList = eventList.stream()
                                                    .map(EventMapper::mapEventToShortDto)
                                                    .collect(Collectors.toList());
        // Получаем список ID для найденных Event
        List<Long> eventIds = eventList.stream().map(Event::getId).collect(Collectors.toList());
        // Получаем все подтверждённые запросы для этих Event
        List<ParticipationRequest> reqList = requestRepository.getByEvent_IdInAndStatus(eventIds, RequestStatus.CONFIRMED);
        // Считаем для каждого Event кол-во подтверждённых запросов
        eventDtoList.forEach(eventDto -> {
                                            eventDto.setConfirmedRequests(
                                                reqList.stream()
                                                    .filter(r -> r.getEvent().getId().equals(eventDto.getId()))
                                                    .count()
                                            );
                                          });
        // Дописать обогащение статистикой и сделать фильтр - события у которых есть свободные места
        return eventDtoList;
    }

    // Получение подробной информации об опубликованном событии по его идентификатору (для Public контроллера)
    // Обратите внимание:
    //
    //    событие должно быть опубликовано
    //    информация о событии должна включать в себя количество просмотров и количество подтвержденных запросов
    //    информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики
    //
    //В случае, если события с заданным id не найдено, возвращает статус код 404
    @Override
    @Transactional
    public EventFullDto getEventById(Long id) {
        Event event = eventRepository.findById(id)
                                     .orElseThrow(() -> new DataNotFoundException("Event not found", HttpStatus.NOT_FOUND));

        if (!event.getState().equals(EventState.PUBLISHED)) {
           throw new ValidationException("Event not published", HttpStatus.NOT_FOUND);
        }
        EventFullDto fullDto = EventMapper.mapEventToDto(event);

        fullDto.setConfirmedRequests(requestRepository.getByEvent_IdInAndStatus(Arrays.asList(id), RequestStatus.CONFIRMED).stream().count());

        //заполнить поле views в DTO
        return fullDto;
    }

    // Получение событий, добавленных текущим пользователем (для Private контроллера)
    // В случае, если по заданным фильтрам не найдено ни одного события, возвращает пустой список
    @Override
    @Transactional
    public List<EventShortDto> getUserEvent(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from, size);
        // сделать получение подтверждённых заявок на участие и просмотров событий
        return eventRepository.getByInitiator_Id(userId, pageable).stream()
            .map(EventMapper::mapEventToShortDto)
            .collect(Collectors.toList());
    }

    // Добавление нового события (для Private контроллера)
    // Обратите внимание: дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента
    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        LocalDateTime now = LocalDateTime.now();

        if(now.plusHours(2L).isAfter(newEventDto.getEventDate())) {
            throw new ValidationException("Time validation error", HttpStatus.CONFLICT);
        }
        User initiator = userRepository.findById(userId)
                                       .orElseThrow(() -> new DataNotFoundException("User not found", HttpStatus.NOT_FOUND));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                                              .orElseThrow(() -> new DataNotFoundException("Category not found", HttpStatus.NOT_FOUND));

        Event event = EventMapper.mapDtoToEvent(newEventDto);

        Location loc = locationRepository.findByLatAndLon(newEventDto.getLocation().getLat(), newEventDto.getLocation().getLon())
            .orElse(locationRepository.save(newEventDto.getLocation()));

        event.setInitiator(initiator);
        event.setLocation(loc);
        event.setCategory(category);
        event.setCreatedOn(now);
        event.setState(EventState.PENDING);
        return EventMapper.mapEventToDto(eventRepository.save(event));
    }

    // Получение полной информации о событии добавленном текущим пользователем (для Private контроллера)
    // В случае, если события с заданным id не найдено, возвращает статус код 404
    @Override
    @Transactional
    public EventFullDto getEventById(Long userId, Long eventId) {
        User initiator = userRepository.findById(userId)
                                       .orElseThrow(() -> new DataNotFoundException("User not found", HttpStatus.NOT_FOUND));

        Event event = eventRepository.findById(eventId)
                                     .orElseThrow(() -> new DataNotFoundException("Event not found", HttpStatus.NOT_FOUND));
        return EventMapper.mapEventToDto(event);
    }

    // Изменение события добавленного текущим пользователем (для Private контроллера)
    // Обратите внимание:
    //
    //    изменить можно только отмененные события или события в состоянии ожидания модерации (Ожидается код ошибки 409)
    //    дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента (Ожидается код ошибки 409)
    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updUserRequest) {
        LocalDateTime now = LocalDateTime.now();

        User initiator = userRepository.findById(userId)
                                       .orElseThrow(() -> new DataNotFoundException("User not found", HttpStatus.NOT_FOUND));

        Event event = eventRepository.findById(eventId)
                                     .orElseThrow(() -> new DataNotFoundException("Event not found", HttpStatus.NOT_FOUND));

        if (!initiator.equals(event.getInitiator())) {
            throw new ValidationException("You're not event owner", HttpStatus.CONFLICT);
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Incorrect status", HttpStatus.CONFLICT);
        }

        if (updUserRequest.getAnnotation() != null) {
            event.setAnnotation(updUserRequest.getAnnotation());
        }
        Long catId = updUserRequest.getCategory();

        if (catId != null && !catId.equals(event.getCategory().getId())) {
            Category category = categoryRepository.findById(catId)
                                                  .orElseThrow(() -> new DataNotFoundException("Category not found", HttpStatus.NOT_FOUND));

            event.setCategory(category);
        }
        if (updUserRequest.getDescription() != null) {
            event.setDescription(updUserRequest.getDescription());
        }
        if (updUserRequest.getEventDate() != null) {
            if(now.plusHours(2L).isAfter(updUserRequest.getEventDate())) {
                throw new ValidationException("Time validation error", HttpStatus.CONFLICT);
            }
            event.setEventDate(updUserRequest.getEventDate());
        }
        if (updUserRequest.getLocation() != null) {
            event.setLocation(updUserRequest.getLocation());
        }
        if (updUserRequest.getPaid() != null) {
            event.setPaid(updUserRequest.getPaid());
        }
        if (updUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updUserRequest.getParticipantLimit());
        }
        if (updUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updUserRequest.getRequestModeration());
        }
        if (updUserRequest.getStateAction() != null) {
            if (updUserRequest.getStateAction().equals(UserStateAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            }
            if (updUserRequest.getStateAction().equals(UserStateAction.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            }
        }
        return EventMapper.mapEventToDto(eventRepository.save(event));
    }

    // Поиск событий (для Admin контроллера)
    // Эндпоинт возвращает полную информацию обо всех событиях подходящих под переданные условия
    //
    //В случае, если по заданным фильтрам не найдено ни одного события, возвращает пустой список
    @Override
    @Transactional
    public List<EventFullDto> getEventsByAdmin(Long[] users,
                                               EventState[] states,
                                               Long[] categories,
                                               LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd,
                                               int from,
                                               int size) {
        List<Long> userList = null, categoryList = null;

        List<EventState> stateList = null;

        if (users != null) {
            userList = Arrays.asList(users);
        }
        if (categories != null) {
            categoryList = Arrays.asList(categories);
        }
        if (states != null) {
            stateList = Arrays.asList(states);
        }
        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
            rangeEnd = LocalDateTime.now().plusYears(1000L);
        }
        Pageable pageable = PageRequest.of(from, size, Sort.by("eventDate"));

        List<Event> eventList = eventRepository.findEventsForAdmin(userList, stateList, categoryList, rangeStart, rangeEnd, pageable);
        // Дописать обогащение статистикой и сделать фильтр - события у которых есть свободные места

        List<EventFullDto> eventDtoList = eventList.stream().map(EventMapper::mapEventToDto).collect(Collectors.toList());

        List<ParticipationRequest> reqList = requestRepository.getByEvent_IdInAndStatus(Arrays.asList(), RequestStatus.CONFIRMED);

        eventDtoList.forEach(eventDto -> {
                                          eventDto.setConfirmedRequests(reqList.stream()
                                                                                .filter(r -> r.getEvent().getId() == eventDto.getId())
                                                                                .count());
                                          });
        return eventDtoList;
    }

    // Редактирование данных события и его статуса (отклонение/публикация) (для Admin контроллера)
    // Редактирование данных любого события администратором. Валидация данных не требуется. Обратите внимание:
    //
    //    дата начала изменяемого события должна быть не ранее чем за час от даты публикации. (Ожидается код ошибки 409)
    //    событие можно публиковать, только если оно в состоянии ожидания публикации (Ожидается код ошибки 409)
    //    событие можно отклонить, только если оно еще не опубликовано (Ожидается код ошибки 409)
    @Override
    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updAdminRequest) {
        LocalDateTime now = LocalDateTime.now();

        Event event = eventRepository.findById(eventId)
                                     .orElseThrow(() -> new DataNotFoundException("Event not found", HttpStatus.NOT_FOUND));

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Incorrect status", HttpStatus.CONFLICT);
        }

        if (updAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updAdminRequest.getAnnotation());
        }

        if (updAdminRequest.getCategory() != null && ! updAdminRequest.getCategory().equals(event.getCategory().getId())) {
            Category category = categoryRepository.findById( updAdminRequest.getCategory())
                .orElseThrow(() -> new DataNotFoundException("Category not found", HttpStatus.NOT_FOUND));

            event.setCategory(category);
        }
        if (updAdminRequest.getDescription() != null) {
            event.setDescription(updAdminRequest.getDescription());
        }
        if (updAdminRequest.getEventDate() != null) {
            if(now.plusHours(1L).isAfter(updAdminRequest.getEventDate())) {
                throw new ValidationException("Time validation error", HttpStatus.CONFLICT);
            }
            event.setEventDate(updAdminRequest.getEventDate());
        }
        if (updAdminRequest.getLocation() != null) {
            event.setEventDate(updAdminRequest.getEventDate());
        }
        if (updAdminRequest.getPaid() != null) {
            event.setPaid(updAdminRequest.getPaid());
        }
        if (updAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updAdminRequest.getParticipantLimit());
        }
        if (updAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updAdminRequest.getRequestModeration());
        }
        if (updAdminRequest.getStateAction() != null) {
            if (updAdminRequest.getStateAction().equals(AdminStateAction.PUBLISH_EVENT)) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(now);
            }
            if (updAdminRequest.getStateAction().equals(AdminStateAction.REJECT_EVENT)) {
                if (event.getState().equals(EventState.PUBLISHED)) {
                    throw new ValidationException("Wrong state of event", HttpStatus.CONFLICT);
                }
                event.setState(EventState.CANCELED);
            }
        }
        return EventMapper.mapEventToDto(eventRepository.save(event));
    }

}
