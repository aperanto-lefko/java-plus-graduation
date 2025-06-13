package ru.practicum.producer;

import kafka.exception.SerializationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.springframework.stereotype.Service;
import ru.practicum.exception.SendMessageException;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KafkaProducerService {
    final KafkaProducer<String, SpecificRecordBase> kafkaProducer;

    public void send(SpecificRecordBase data, String topic) {
        try {
            kafkaProducer.send(new ProducerRecord<>(topic, data),
                    (metadata, e) -> {
                        if (e != null) {
                            log.error("[{}] Ошибка отправки: {}", topic, e.getMessage());
                        } else {
                            log.info("Отправлено в {} - {}", topic, metadata.partition());
                        }
                    });
        } catch (SerializationException | KafkaException ex) {
            log.error("Ошибка при отправлении сообщения:", ex);
            throw new SendMessageException("Ошибка при отправлении сообщения", ex);
        }
    }
}
