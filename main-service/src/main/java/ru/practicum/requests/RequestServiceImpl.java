package ru.practicum.requests;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.events.EventRepository;
import ru.practicum.events.models.Event;
import ru.practicum.events.models.EventState;
import ru.practicum.exceptions.DataNotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.dto.RequestStatus;
import ru.practicum.requests.models.ParticipationRequest;
import ru.practicum.users.UserRepository;
import ru.practicum.users.models.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Autowired
    public RequestServiceImpl(RequestRepository requestRepository, EventRepository eventRepository,
                              UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    // Получение информации о запросах на участие в событии текущего пользователя (для Private Event контроллера)
    // В случае, если по заданным фильтрам не найдено ни одной заявки, возвращает пустой список
    @Override
    @Transactional
    public List<ParticipationRequestDto> getRequestForEvent(Long userId, Long eventId) {
        List<ParticipationRequest> requests = requestRepository.findByEvent_Id(eventId);

        return requests.stream().map(RequestMapper::mapReqToDto).collect(Collectors.toList());
    }

    // Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя (для Private Event контроллера)
    // Обратите внимание:
    //
    //    если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется
    //    нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие (Ожидается код ошибки 409)
    //    статус можно изменить только у заявок, находящихся в состоянии ожидания (Ожидается код ошибки 409)
    //    если при подтверждении данной заявки, лимит заявок для события исчерпан, то все неподтверждённые заявки необходимо отклонить
    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId,
                                                              Long eventId,
                                                              EventRequestStatusUpdateRequest statusUpdRequest) {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new DataNotFoundException("User not found", HttpStatus.NOT_FOUND));

        Event event = eventRepository.findById(eventId)
                                     .orElseThrow(() -> new DataNotFoundException("Event not found", HttpStatus.NOT_FOUND));

        // получаем из БД список заявок на участие
        List<ParticipationRequest> requestList = requestRepository.findAllById(statusUpdRequest.getRequestIds());

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();

        if (statusUpdRequest.getStatus().equals(RequestStatus.REJECTED)) {
            requestList.forEach(r -> {
                                        if (r.getStatus().equals(RequestStatus.CONFIRMED)) {
                                            throw new ValidationException("Request confirmed and can't be rejected", HttpStatus.CONFLICT);
                                        }
                                        r.setStatus(RequestStatus.REJECTED);
                                      });
            requestRepository.saveAll(requestList);
            result.setRejectedRequests(requestList.stream().map(RequestMapper::mapReqToDto).collect(Collectors.toList()));
            return result;
        }
        // тут надо получить лимит по участникам для события и проверить сколько заявок на участие уже создано.
        long reqCount = requestRepository.findByEvent_IdInAndStatus(Arrays.asList(eventId), RequestStatus.CONFIRMED).stream().count() + 1;

        long reqLimit = event.getParticipantLimit();

        if (reqCount > reqLimit) {
            throw new ValidationException("Request limit reached", HttpStatus.CONFLICT);
        }

        List<Long> confirmed = new ArrayList<>();

        List<Long> rejected = new ArrayList<>();

        if (statusUpdRequest.getStatus().equals(RequestStatus.CONFIRMED) && reqLimit != 0) {
            for (Long id : statusUpdRequest.getRequestIds()) {
                if (reqCount > reqLimit) {
                    rejected.add(id);
                    break;
                } else {
                    confirmed.add(id);
                }
                reqCount++;
            }
        }
        requestList.forEach(req -> {
                                    if (confirmed.contains(req.getId())) {
                                        req.setStatus(RequestStatus.CONFIRMED);
                                        result.getConfirmedRequests().add(RequestMapper.mapReqToDto(req));
                                    } else {
                                        req.setStatus(RequestStatus.REJECTED);
                                        result.getRejectedRequests().add(RequestMapper.mapReqToDto(req));
                                    }
                                });
        return result;
    }

    // Получение информации о заявках текущего пользователя на участие в чужих событиях
    // В случае, если по заданным фильтрам не найдено ни одной заявки, возвращает пустой список
    @Override
    @Transactional
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        List<ParticipationRequest> reqList = requestRepository.findByRequester_Id(userId);

        return reqList.stream().map(RequestMapper::mapReqToDto).collect(Collectors.toList());
    }

    // Добавление запроса от текущего пользователя на участие в событии
    // Обратите внимание:
    //
    //    нельзя добавить повторный запрос (Ожидается код ошибки 409)
    //    инициатор события не может добавить запрос на участие в своём событии (Ожидается код ошибки 409)
    //    нельзя участвовать в неопубликованном событии (Ожидается код ошибки 409)
    //    если у события достигнут лимит запросов на участие - необходимо вернуть ошибку (Ожидается код ошибки 409)
    //    если для события отключена пре-модерация запросов на участие, то запрос должен автоматически перейти в состояние подтвержденного
    @Override
    @Transactional
    public ParticipationRequestDto addUserRequestToEvent(Long userId, Long eventId) {
        LocalDateTime now = LocalDateTime.now();

        if (requestRepository.findByEvent_IdAndRequester_Id(eventId, userId).isPresent()) {
            throw new ValidationException("Request already exist", HttpStatus.CONFLICT);
        }
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new DataNotFoundException("User not found", HttpStatus.NOT_FOUND));

        Event event = eventRepository.findById(eventId)
                                     .orElseThrow(() -> new DataNotFoundException("Event not found", HttpStatus.NOT_FOUND));

        if (user.equals(event.getInitiator())) {
            throw new ValidationException("You can not create request for own event", HttpStatus.CONFLICT);
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Event not published", HttpStatus.CONFLICT);
        }
        ParticipationRequest request = new ParticipationRequest(null, now, event, user, RequestStatus.PENDING);

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
        }
        // Получаем кол-во подтверждённых заявок на участие в событии.
//        long reqCount = requestRepository.countByEvent_Id(eventId).orElse(0L);
        long reqCount = requestRepository.findByEvent_IdInAndStatus(Arrays.asList(eventId), RequestStatus.CONFIRMED).stream().count() + 1;

        if (event.getParticipantLimit() != 0 && reqCount > event.getParticipantLimit()) {
            throw new ValidationException("Request limit reached", HttpStatus.CONFLICT);
        }
        return RequestMapper.mapReqToDto(requestRepository.save(request));
    }

    // Отмена своего запроса на участие в событии
    @Override
    @Transactional
    public ParticipationRequestDto cancelUserRequestToEvent(Long userid, Long requestId) {
        ParticipationRequest request =  requestRepository.findById(requestId)
                                                         .orElseThrow(() -> new DataNotFoundException("Request not found", HttpStatus.NOT_FOUND));

        User user = userRepository.findById(userid)
                                  .orElseThrow(() -> new DataNotFoundException("User not found", HttpStatus.NOT_FOUND));

        if (!request.getRequester().equals(user)) {
            throw new ValidationException("You can't cancel foreign request", HttpStatus.CONFLICT);
        }
        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.mapReqToDto(requestRepository.save(request));
    }
}
