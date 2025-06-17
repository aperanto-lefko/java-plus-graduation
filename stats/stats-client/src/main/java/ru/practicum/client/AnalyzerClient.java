package ru.practicum.client;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.evm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.evm.stats.proto.RecommendedEventProto;
import ru.practicum.evm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.evm.stats.proto.UserPredictionsRequestProto;
import ru.practicum.ewm.stats.proto.RecommendationsControllerGrpc;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class AnalyzerClient {
    @GrpcClient("analyzer")
    RecommendationsControllerGrpc.RecommendationsControllerBlockingStub blockingStub;

    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResult) {
        UserPredictionsRequestProto proto = UserPredictionsRequestProto
                .newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResult)
                .build();
        Iterator<RecommendedEventProto> iterator = blockingStub.getRecommendationsForUser(proto);
        return asStream(iterator);
    }

    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        InteractionsCountRequestProto proto = InteractionsCountRequestProto
                .newBuilder()
                .addAllEventId(eventIds)
                .build();
        Iterator<RecommendedEventProto> iterator = blockingStub.getInteractionsCount(proto);
        return asStream(iterator);
    }

    public Stream<RecommendedEventProto> getSimilarEvents(long userId, long eventId, int maxResults) {
        SimilarEventsRequestProto request = SimilarEventsRequestProto
                .newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setMaxResults(maxResults)
                .build();
        Iterator<RecommendedEventProto> iterator = blockingStub.getSimilarEvents(request);
        return asStream(iterator);
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }

}
