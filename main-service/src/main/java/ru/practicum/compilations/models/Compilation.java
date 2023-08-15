package ru.practicum.compilations.models;

import lombok.*;
import ru.practicum.events.models.Event;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "compilations")
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;            // Идентификатор
    @Column(name = "title")
    private String title;       // Заголовок подборки
    @Column(name = "pinned")
    private Boolean pinned;     // Закреплена ли подборка на главной странице сайта
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "comp_event_binding",
               joinColumns = @JoinColumn(name = "comp_id"),
               inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    List<Event> eventList;
}
