package ru.practicum.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.practicum.client.StatClient;
import ru.practicum.dto.HitDto;

import java.time.LocalDateTime;

@Component
@AllArgsConstructor
public class RequestInterceptor implements HandlerInterceptor {

    private final StatClient statClient;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        statClient.hit(new HitDto("ewm-main-service", request.getRequestURI(), request.getRemoteAddr(),
                LocalDateTime.now()));
        return true;
    }
}