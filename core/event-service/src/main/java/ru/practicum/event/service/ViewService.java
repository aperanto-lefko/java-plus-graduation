package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.event.model.Event;

import java.util.List;

public interface ViewService {
    void saveViews(List<Event> events, HttpServletRequest rqt);

    void saveView(Event ev, HttpServletRequest rqt);
}
