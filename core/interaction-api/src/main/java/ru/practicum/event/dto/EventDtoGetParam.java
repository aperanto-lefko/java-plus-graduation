package ru.practicum.event.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@ToString
public class EventDtoGetParam {
    private Integer size = 10;
    private Integer from = 0;
    private Long userId;
    private Long eventId;
    private List<Long> users; //список id пользователей
    private List<String> states; //список состояний событий
    private List<Long> categories;//список id категорий
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;
    private String text;
    private Boolean paid;
    private Boolean onlyAvailable = false; //только события у которых не исчерпан лимит запросов на участие
    private String sort;
}
