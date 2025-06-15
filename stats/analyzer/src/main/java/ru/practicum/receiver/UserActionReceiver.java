package ru.practicum.receiver;


import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.UserActionService;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserActionReceiver extends BaseKafkaReceiver<UserActionAvro> {

    @Value("${kafka.topics.user-actions}")
    String inputTopic;
    final UserActionService userActionService;

    public UserActionReceiver(KafkaConsumer<String, UserActionAvro> consumer,
                              UserActionService userActionService) {
        super(consumer);
        this.userActionService=userActionService;
    }

    @Override
    protected String getInputTopic() {
        return inputTopic;
    }

    @Override
    protected void processMessage(UserActionAvro message) {
        userActionService.process(message);
    }
}
