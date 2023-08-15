package ru.practicum.events.models;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

// Широта и долгота места проведения события
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // Идентификатор
    @NotNull
    private float lat;
    @NotNull
    private float lon;
}
