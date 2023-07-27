package ru.practicum.models;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "uri_statistics")
public class StatEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "application")
    private String app;
    @Column(name = "uri")
    private String uri;
    @Column(name = "ip_address")
    private String ip;
    @Column(name = "created")
    private LocalDateTime timestamp;
}
