package ru.practicum.requests;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.events.models.Event;
import ru.practicum.requests.dto.RequestStatus;
import ru.practicum.requests.models.ParticipationRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    Optional<ParticipationRequest> findByEvent_IdAndRequester_Id(Long eventId, Long requesterId);

//    @Query(value = "select count(req.id) from ParticipationRequest as req where req.status = 'CONFIRMED'")
    List<ParticipationRequest> getByEvent_IdInAndStatus(List<Long> eventIds, RequestStatus status);

    Long countByEvent_IdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findByRequester_Id(Long requesterId);
}
