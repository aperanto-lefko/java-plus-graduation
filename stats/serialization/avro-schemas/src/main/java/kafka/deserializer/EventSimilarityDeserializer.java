package kafka.deserializer;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;


public class EventSimilarityDeserializer extends BaseAvroDeserializer <EventSimilarityAvro> {
    public EventSimilarityDeserializer() {
        super(EventSimilarityAvro.class);
    }
}
