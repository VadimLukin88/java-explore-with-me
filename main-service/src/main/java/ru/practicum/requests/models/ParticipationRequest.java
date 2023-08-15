package ru.practicum.requests.models;


import lombok.*;
import ru.practicum.events.models.Event;
import ru.practicum.requests.dto.RequestStatus;
import ru.practicum.users.models.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "participation_requests")
@Builder
public class ParticipationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;            // Идентификатор заявки
    @Column(name = "created")
    private LocalDateTime created;     // Дата и время создания заявки
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;         // Идентификатор события
    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;     // Идентификатор пользователя, отправившего заявку
    @Enumerated(value = EnumType.STRING)
    @Column(name = "status")
    private RequestStatus status;      // Какие значения должны быть тут ??? example: PENDING
}
