package ru.practicum.client;

import com.google.protobuf.Timestamp;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.evm.stats.proto.ActionTypeProto;
import ru.practicum.evm.stats.proto.UserActionProto;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;

import java.time.Instant;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CollectorClient {
    final UserActionControllerGrpc.UserActionControllerBlockingStub blockingStub;

    public CollectorClient(@GrpcClient("collector") UserActionControllerGrpc.UserActionControllerBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    public void sendUserAction(Long userId, Long eventId) {
        Instant instant = Instant.now();
        Timestamp grpcTimestamp = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
        UserActionProto proto = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(ActionTypeProto.ACTION_REGISTER)
                .setTimestamp(grpcTimestamp)
                .build();

        try {
            blockingStub.collectUserAction(proto);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
