package ru.practicum.events;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.events.models.Event;
import ru.practicum.events.models.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> getByInitiator_Id(Long initiatorId, Pageable pageable);

    @Query(value = "SELECT e FROM Event AS e WHERE "
                    + "(LOWER(e.annotation) LIKE %:text% OR LOWER(e.description) LIKE %:text%) "
                    + "AND (:categories IS NULL OR e.category IN (SELECT c FROM Category as c WHERE e.id IN :categories)) "
                    + "AND (:paid IS NULL OR e.paid = :paid) "
                    + "AND (e.eventDate > :rangeStart) "
                    + "AND (e.eventDate < :rangeEnd) "
                    + "AND (e.state = 'PUBLISHED')")
    List<Event> findEventsForUser(String text,
                                  List<Long> categories,
                                  boolean paid,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd,
                                  Pageable pageable);

    @Query(value = "SELECT e FROM Event AS e WHERE "
        + "(:users IS NULL OR e.initiator IN (SELECT u FROM User AS u WHERE u.id IN :users)) "
        + "AND (:states IS NULL OR e.state IN :states) "
        + "AND (:categories IS NULL OR e.category IN (SELECT c FROM Category as c WHERE c.id IN :categories)) "
        + "AND (e.eventDate > :rangeStart) "
        + "AND (e.eventDate < :rangeEnd) ")
    List<Event> findEventsForAdmin(List<Long> users,
                                   List<EventState> states,
                                   List<Long> categories,
                                   LocalDateTime rangeStart,
                                   LocalDateTime rangeEnd,
                                   Pageable pageable);
}
