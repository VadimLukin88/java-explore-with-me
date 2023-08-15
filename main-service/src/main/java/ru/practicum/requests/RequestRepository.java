package ru.practicum.requests;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.requests.dto.RequestStatus;
import ru.practicum.requests.models.ParticipationRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    Optional<ParticipationRequest> findByEvent_IdAndRequester_Id(Long eventId, Long requesterId);

//    @Query(value = "select count(req.id) from ParticipationRequest as req where req.status = 'CONFIRMED'")
    List<ParticipationRequest> findByEvent_IdInAndStatus(List<Long> eventIds, RequestStatus status);

    List<ParticipationRequest> findByEvent_Id(Long eventId);

    Optional<Long> countByEvent_IdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findByRequester_Id(Long requesterId);

    Optional<Long> countByEvent_Id(Long eventId);


}