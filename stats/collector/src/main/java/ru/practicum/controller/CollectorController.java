package ru.practicum.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import ru.practicum.evm.stats.proto.UserActionProto;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.exception.SendMessageException;
import ru.practicum.kafka_commons.KafkaCollectorProducer;
import ru.practicum.mapper.UserActionMapper;

@GrpcService
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CollectorController extends UserActionControllerGrpc.UserActionControllerImplBase {
    @Value("${kafka.topics.user-actions}")
    private String topic;
    final KafkaCollectorProducer kafkaCollectorProducer;
    final UserActionMapper mapper;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Обработка контроллером collectUserAction сообщения UserActionProto {}", request);
            UserActionAvro avro = mapper.mapToAvro(request);
            kafkaCollectorProducer.send(avro, topic);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
            log.debug("Успешная обработка события {}", request);
        } catch (SendMessageException e) {
            String errorDetails = String.format("Ошибка обработки события %s: %s", request, e.getMessage());
            Status status = Status.INTERNAL.withDescription(errorDetails);
            responseObserver.onError(new StatusRuntimeException(status));
        }
    }
}
