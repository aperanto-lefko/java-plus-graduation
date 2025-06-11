package kafka.serializer;

import kafka.exception.NullValueException;
import kafka.exception.SerializationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AvroSerializer implements Serializer<SpecificRecordBase> {
    private final EncoderFactory encoderFactory = EncoderFactory.get();
    // Кэш для DatumWriter, чтобы избежать повторного создания для одной и той же схемы.
    private final Map<Schema, DatumWriter<SpecificRecordBase>> writers = new HashMap<>();

    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        if (data == null) {
            log.error("Данные для cериализации равны null");
            throw new NullValueException("Данные для сериализации равны null");
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = encoderFactory.binaryEncoder(out, null);
            Schema schema = data.getSchema();
            // Используем кэшированный DatumWriter или создаем новый, если его нет в кэше.
            DatumWriter<SpecificRecordBase> writer = writers.computeIfAbsent(
                    schema,
                    s -> {
                        log.debug("Создан новый DatumWriter для схемы {}", schema);
                        return new SpecificDatumWriter<>(s);
                    });
            writer.write(data, encoder);
            encoder.flush();
            log.debug("Данные успешно сериализованы для топика {}", topic);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new SerializationException("Ошибка сериализации данных для топика " + topic, ex);
        }
    }
}
