package ru.practicum.event.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Formula;
import ru.practicum.category.model.Category;
import ru.practicum.event.validate.TimeAtLeastTwoHours;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "events")
@Builder(toBuilder = true)
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User initiator;
    @ManyToOne(cascade = {CascadeType.ALL}) //при сохранении event,сохранится location
    @JoinColumn(name = "location_id")
    private Location location;
    @NotBlank
    @Column(name = "title")
    private String title;
    @Column(name = "annotation")
    private String annotation;
    @Column(name = "description")
    private String description;
    @Formula("(SELECT COUNT(*) FROM requests r WHERE r.event_id = id AND r.status = 'CONFIRMED')")
    private int confirmedRequests;
    @Column(name = "participant_limit")
    //Ограничение на количество участников. Значение 0 - означает отсутствие ограничения
    private int participantLimit; //
    @Formula("(SELECT COUNT(*) FROM views v WHERE v.event_id = id)")
    private int views; //просмотры события*/
    @Column(name = "request_moderation")
    @Builder.Default
    private Boolean requestModeration = true; //Нужна ли пре-модерация заявок на участие
    @NotNull
    private Boolean paid;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_on")
    private LocalDateTime createdOn; //Дата и время создания события
    @TimeAtLeastTwoHours
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "event_date")
    private LocalDateTime eventDate; //Дата и время на которые намечено событие
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "published_on")
    private LocalDateTime publishedOn; //Дата и время публикации события
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private State state;
}
