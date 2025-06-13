package kafka.deserializer;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public class UserActionDeserializer extends BaseAvroDeserializer <UserActionAvro>{
    public UserActionDeserializer() {
        super(UserActionAvro.class);
    }
}
