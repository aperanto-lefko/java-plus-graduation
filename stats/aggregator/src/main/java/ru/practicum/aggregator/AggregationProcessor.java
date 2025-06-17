package ru.practicum.aggregator;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AggregationProcessor {
    final EventSimilarityCalculator matrix;

    public List<EventSimilarityAvro> process(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();
        double weight = getActionWeight(action.getActionType());

        return matrix.updateWeights(userId, eventId, weight);
    }

    private double getActionWeight(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 0.4;
            case LIKE -> 1.0;
            case REGISTER -> 0.8;
        };
    }
}
