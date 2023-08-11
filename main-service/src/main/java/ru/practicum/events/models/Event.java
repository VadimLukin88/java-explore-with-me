package ru.practicum.events.models;

import lombok.*;
import ru.practicum.categories.models.Category;
import ru.practicum.requests.models.ParticipationRequest;
import ru.practicum.users.models.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // Идентификатор
    @Column(name = "title")
    private String title;               // Заголовок
    @Column(name = "annotation")
    private String annotation;          // Краткое описание
    @Column(name = "description")
    private String description;         // Полное описание события
    @Column(name = "event_date")
    private LocalDateTime eventDate;    // Дата и время на которые намечено событие
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;          // Категория
    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private User initiator;             // Пользователь (инициатор события)
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;          // Широта и долгота места проведения события
    @Column(name = "paid")
    private Boolean paid;               // Нужно ли оплачивать участие
    @Column(name = "participant_limit")
    private Integer participantLimit;       // Default = 0. Ограничение на количество участников. Значение 0 - означает отсутствие ограничения
    @Column(name = "request_moderation")
    private Boolean requestModeration;  // Нужна ли пре-модерация заявок на участие. Default = true
    @Column(name = "state")
    @Enumerated(value = EnumType.STRING)
    private EventState state;   // Список состояний жизненного цикла события. enum
    @Column(name = "created_on")
    private LocalDateTime createdOn;        // Дата и время создания события
    @Column(name = "published_on")
    private LocalDateTime publishedOn;      // Дата и время публикации события
    // Написать чтобы можно было получать список запросов на участие
    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    List<ParticipationRequest> requestList;
}
