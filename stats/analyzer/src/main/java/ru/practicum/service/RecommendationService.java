package ru.practicum.service;

import ru.practicum.dto.RecommendedEvent;

import java.util.List;

public interface RecommendationService {
    List<RecommendedEvent> getRecommendationsForUser(long userId, int maxResults);
    List<RecommendedEvent> getSimilarEvents(long userId, long eventId);
    List<RecommendedEvent> getInteractionsCount(List<Long> eventIds);
}
