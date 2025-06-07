package ru.practicum.event.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "views")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class View {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ip;
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
    @Column(name = "view_time")
    private LocalDateTime viewTime;
}
