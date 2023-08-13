package ru.practicum.events;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatClient;
import ru.practicum.categories.CategoryRepository;
import ru.practicum.categories.models.Category;
import ru.practicum.dto.StatRequestDto;
import ru.practicum.dto.StatResponseDto;
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

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventServiceImpl implements EventsService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final StatClient statClient;

    private final ObjectMapper objectMapper;
    private static final String APPLICATION = "main-service";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository,
                            CategoryRepository categoryRepository, LocationRepository locationRepository,
                            RequestRepository requestRepository, StatClient statClient, ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
        this.requestRepository = requestRepository;
        this.statClient = statClient;
        this.objectMapper = objectMapper;
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
                                          int size,
                                          HttpServletRequest request) {
        String searchText = null;

        List<Long> catList = null;

        if (text != null) {
            searchText = "%" + text.toLowerCase() + "%";
        }
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now().minusYears(1000L);
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(1000L);
        }
        if (rangeEnd.isBefore(rangeStart)) {
            throw new ValidationException("Start time after end time", HttpStatus.BAD_REQUEST);
        }
        if (categories != null && categories.length > 0) {
            catList = Arrays.asList(categories);
        }

        Pageable pageable;

        if (sort.equals(EventSort.EVENT_DATE)) {
            pageable = PageRequest.of(from, size, Sort.by("eventDate"));
        } else {
            pageable = PageRequest.of(from, size);
        }
        // Ищем события по фильтру
        List<Event> eventList = eventRepository.findEventsForUser(searchText, catList, paid, rangeStart, rangeEnd, pageable);
        // Получаем список ID для найденных Event
        List<Long> eventIds = eventList.stream().map(Event::getId).collect(Collectors.toList());
        // Получаем все подтверждённые запросы для этих Event
        List<ParticipationRequest> reqList = requestRepository.findByEvent_IdInAndStatus(eventIds, RequestStatus.CONFIRMED);
        // Считаем сколько подтверждённых заявок у каждого Event
        Map<Long, Long> reqMap = reqList.stream().collect(Collectors.groupingBy(r -> r.getEvent().getId(), Collectors.counting()));
        // Аналогично для просмотров
        String[] uris = eventIds.stream().map(eId -> "/events/" + eId).toArray(String[]::new);
        // Получаем просмотры из сервера статистики
        Object statResult = statClient.getStats(rangeStart.format(TIME_FORMATTER), rangeEnd.format(TIME_FORMATTER), uris,true)
                                      .getBody();

        List<StatResponseDto> statRespDto = objectMapper.convertValue(statResult, new TypeReference<List<StatResponseDto>>() {});
        // Считаем просмотры для каждого Event
        Map<String, Long> uriHits = statRespDto.stream().collect(Collectors.toMap(sr -> sr.getUri(), sr -> sr.getHits()));

        // Преобразуем Event в DTO
        List<EventShortDto> eventDtoList;

        if (onlyAvailable) {
            eventDtoList = eventList.stream()
                .filter(event -> event.getParticipantLimit() == 0 || event.getParticipantLimit() < reqMap.getOrDefault(event.getId(), 0L))
                .map(EventMapper::mapEventToShortDto)
                .collect(Collectors.toList());
        } else {
            eventDtoList = eventList.stream()
                .map(EventMapper::mapEventToShortDto)
                .collect(Collectors.toList());
        }

        // Для каждого Event заполняем кол-во подтверждённых запросов и просмотров
        eventDtoList.forEach(eventDto -> {
                                            eventDto.setConfirmedRequests(reqMap.getOrDefault(eventDto.getId(), 0L));
                                            eventDto.setViews(uriHits.getOrDefault("/events/" + eventDto.getId(), 0L));
                                          });
        // Отправляем запрос в сервер статистики
        try {
            StatRequestDto statDto = new StatRequestDto(APPLICATION,
                                                        request.getRequestURI(),
                                                        request.getRemoteAddr(),
                                                        LocalDateTime.now().format(TIME_FORMATTER));
            statClient.hitStats(statDto);
            log.info(">>>> Send request to statistical server. Stat DTO = {}", statDto);
        } catch (Exception e) {
            log.info(">>>> Request to statistical server failed!");
        }
        // пересортируем, если был запрос сортировать по кол-ву просмотров
        if (sort.equals(EventSort.VIEWS)) {
            return eventDtoList.stream()
                               .sorted(Comparator.comparingLong(EventShortDto::getViews).reversed())
                               .collect(Collectors.toList());
        }
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
    public EventFullDto getEventById(Long id, HttpServletRequest request) {
        LocalDateTime rangeStart = LocalDateTime.now().minusYears(1000L);

        LocalDateTime rangeEnd = LocalDateTime.now().plusYears(1000L);

        Event event = eventRepository.findById(id)
                                     .orElseThrow(() -> new DataNotFoundException("Event not found", HttpStatus.NOT_FOUND));

        if (!event.getState().equals(EventState.PUBLISHED)) {
           throw new ValidationException("Event not published", HttpStatus.NOT_FOUND);
        }
        EventFullDto fullDto = EventMapper.mapEventToDto(event);
        // Получаем кол-во подтверждённых запросов
        fullDto.setConfirmedRequests(requestRepository.findByEvent_IdInAndStatus(Arrays.asList(id), RequestStatus.CONFIRMED).stream().count());
        // Получаем просмотры
        String[] uris = new String[]{"/events/" + id};

        Object statResult = statClient.getStats(rangeStart.format(TIME_FORMATTER), rangeEnd.format(TIME_FORMATTER), uris,true)
                                      .getBody();

        List<StatResponseDto> statRespDto = objectMapper.convertValue(statResult, new TypeReference<List<StatResponseDto>>() {});

        if (statRespDto.size() == 0) {
            fullDto.setViews(0L);
        } else {
            fullDto.setViews(statRespDto.get(0).getHits());
        }
        // Отправляем запрос в сервер статистики
        try {
            StatRequestDto statDto = new StatRequestDto(APPLICATION,
                                                        request.getRequestURI(),
                                                        request.getRemoteAddr(),
                                                        LocalDateTime.now().format(TIME_FORMATTER));
            statClient.hitStats(statDto);
            log.info(">>>> Send request to statistical server. Stat DTO = {}", statDto);
        } catch (Exception e) {
            log.info(">>>> Request to statistical server failed!");
        }
        return fullDto;
    }

    // Получение событий, добавленных текущим пользователем (для Private контроллера)
    // В случае, если по заданным фильтрам не найдено ни одного события, возвращает пустой список
    @Override
    @Transactional
    public List<EventShortDto> getUserEvent(Long userId, int from, int size) {
        LocalDateTime rangeStart = LocalDateTime.now().minusYears(1000L);

        LocalDateTime rangeEnd = LocalDateTime.now().plusYears(1000L);

        Pageable pageable = PageRequest.of(from, size);
        // сделать получение подтверждённых заявок на участие и просмотров событий
        List<Event> eventList = eventRepository.getByInitiator_Id(userId, pageable);

        List<Long> eventIds = eventList.stream().map(Event::getId).collect(Collectors.toList());
        // Получаем все подтверждённые запросы для этих Event
        List<ParticipationRequest> reqList = requestRepository.findByEvent_IdInAndStatus(eventIds, RequestStatus.CONFIRMED);
        // Считаем сколько подтверждённых заявок у каждого Event
        Map<Long, Long> reqMap = reqList.stream().collect(Collectors.groupingBy(r -> r.getEvent().getId(), Collectors.counting()));
        // Получаем просмотры
        String[] uris = eventIds.stream().map(eId -> "/events/" + eId).toArray(String[]::new);

        Object statResult = statClient.getStats(rangeStart.format(TIME_FORMATTER), rangeEnd.format(TIME_FORMATTER), uris,true)
                                      .getBody();

        List<StatResponseDto> statRespDto = objectMapper.convertValue(statResult, new TypeReference<List<StatResponseDto>>() {});

        Map<String, Long> uriHits = statRespDto.stream().collect(Collectors.toMap(sr -> sr.getUri(), sr -> sr.getHits()));

        // Преобразуем Event в DTO
        List<EventShortDto> eventDtoList = eventList.stream()
            .map(EventMapper::mapEventToShortDto)
            .collect(Collectors.toList());

        // Заполняем для каждого Event кол-во подтверждённых запросов и просмотров
        eventDtoList.forEach(eventDto -> {
                                            eventDto.setConfirmedRequests(reqMap.getOrDefault(eventDto.getId(), 0L));
                                            eventDto.setViews(uriHits.getOrDefault("/events/" + eventDto.getId(), 0L));
                                         });
        return eventDtoList;
    }

    // Добавление нового события (для Private контроллера)
    // Обратите внимание: дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента
    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        LocalDateTime now = LocalDateTime.now();

        if (now.plusHours(2L).isAfter(newEventDto.getEventDate())) {
            throw new ValidationException("Time validation error", HttpStatus.BAD_REQUEST);
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
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime past = LocalDateTime.now().minusYears(1000L);

        User initiator = userRepository.findById(userId)
                                       .orElseThrow(() -> new DataNotFoundException("User not found", HttpStatus.NOT_FOUND));

        Event event = eventRepository.findById(eventId)
                                     .orElseThrow(() -> new DataNotFoundException("Event not found", HttpStatus.NOT_FOUND));

        EventFullDto fullDto = EventMapper.mapEventToDto(event);
        // Получаем кол-во подтверждённых запросов
        fullDto.setConfirmedRequests(requestRepository.findByEvent_IdInAndStatus(Arrays.asList(eventId), RequestStatus.CONFIRMED).stream().count());

        // Получаем просмотры
        String[] uris = new String[]{"/events/" + eventId};

        Object statResult = statClient.getStats(past.format(TIME_FORMATTER),now.format(TIME_FORMATTER), uris,true)
                                       .getBody();

        List<StatResponseDto> statRespDto = objectMapper.convertValue(statResult, new TypeReference<List<StatResponseDto>>() {});

        if (statRespDto.size() == 0) {
            fullDto.setViews(0L);
        } else {
            fullDto.setViews(statRespDto.get(0).getHits());
        }
        return fullDto;
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
            if (now.plusHours(2L).isAfter(updUserRequest.getEventDate())) {
                throw new ValidationException("Time validation error", HttpStatus.BAD_REQUEST);
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

        LocalDateTime past = LocalDateTime.now().minusYears(1000L);

        LocalDateTime future = LocalDateTime.now().plusYears(1000L);

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
        if (rangeStart == null) {
            rangeStart = past;
        }
        if (rangeEnd == null) {
            rangeEnd = future;
        }
        Pageable pageable = PageRequest.of(from, size, Sort.by("eventDate"));

        List<Event> eventList = eventRepository.findEventsForAdmin(userList, stateList, categoryList, rangeStart, rangeEnd, pageable);
        // Получаем список ID для найденных Event
        List<Long> eventIds = eventList.stream().map(Event::getId).collect(Collectors.toList());
        // Получаем все подтверждённые запросы для этих Event
        List<ParticipationRequest> reqList = requestRepository.findByEvent_IdInAndStatus(eventIds, RequestStatus.CONFIRMED);
        // Считаем сколько подтверждённых заявок у каждого Event
        Map<Long, Long> reqMap = reqList.stream().collect(Collectors.groupingBy(r -> r.getEvent().getId(), Collectors.counting()));

        // Аналогично для просмотров
        String[] uris = eventIds.stream().map(eId -> "/events/" + eId).toArray(String[]::new);
        // Получаем просмотры
        Object statResult = statClient.getStats(past.format(TIME_FORMATTER), future.format(TIME_FORMATTER), uris, true)
                                      .getBody();

        List<StatResponseDto> statRespDto = objectMapper.convertValue(statResult, new TypeReference<List<StatResponseDto>>() {});

        Map<String, Long> uriHits = statRespDto.stream().collect(Collectors.toMap(sr -> sr.getUri(), sr -> sr.getHits()));
        // Получаем кол-во просмотров для событий
        //   statRespDto.forEach(sr -> uriHits.put(sr.getUri(), sr.getHits()));

        // Преобразуем Event в DTO
        List<EventFullDto> eventDtoList = eventList.stream()
            .map(EventMapper::mapEventToDto)
            .collect(Collectors.toList());;
        // Считаем для каждого Event кол-во подтверждённых запросов и просмотров
        eventDtoList.forEach(eventDto -> {
                                            eventDto.setConfirmedRequests(reqMap.getOrDefault(eventDto.getId(), 0L));
                                            eventDto.setViews(uriHits.getOrDefault("/events/" + eventDto.getId(), 0L));
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

        if (event.getState().equals(EventState.PUBLISHED) || event.getState().equals(EventState.CANCELED)) {
            throw new ValidationException("Incorrect status", HttpStatus.CONFLICT);
        }

        if (updAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updAdminRequest.getAnnotation());
        }

        if (updAdminRequest.getCategory() != null && ! updAdminRequest.getCategory().equals(event.getCategory().getId())) {
            Category category = categoryRepository.findById(updAdminRequest.getCategory())
                .orElseThrow(() -> new DataNotFoundException("Category not found", HttpStatus.NOT_FOUND));

            event.setCategory(category);
        }
        if (updAdminRequest.getDescription() != null) {
            event.setDescription(updAdminRequest.getDescription());
        }
        if (updAdminRequest.getEventDate() != null) {
            if (now.plusHours(1L).isAfter(updAdminRequest.getEventDate())) {
                throw new ValidationException("Time validation error", HttpStatus.BAD_REQUEST);
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
        if (updAdminRequest.getTitle() != null) {
            event.setTitle(updAdminRequest.getTitle());
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
