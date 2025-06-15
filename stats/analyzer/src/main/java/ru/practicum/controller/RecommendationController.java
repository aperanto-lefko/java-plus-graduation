package ru.practicum.controller;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.dto.RecommendedEvent;
import ru.practicum.evm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.evm.stats.proto.RecommendedEventProto;
import ru.practicum.evm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.evm.stats.proto.UserPredictionsRequestProto;
import ru.practicum.ewm.stats.proto.RecommendationsControllerGrpc;
import ru.practicum.mapper.RecommendationMapper;
import ru.practicum.service.RecommendationService;

import java.util.List;

@Slf4j
@GrpcService
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecommendationController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    final RecommendationService recommendationService;
    final RecommendationMapper mapper;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            List<RecommendedEvent> recommendations = recommendationService
                    .getRecommendationsForUser(request.getUserId(), request.getMaxResults());

            recommendations.stream()
                    .map(mapper::toProto)
                    .forEach(responseObserver::onNext);

            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка в getRecommendationsForUser", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            List<RecommendedEvent> similarEvents = recommendationService
                    .getSimilarEvents(request.getUserId(), request.getEventId());

            similarEvents.stream()
                    .map(mapper::toProto)
                    .forEach(responseObserver::onNext);

            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in getSimilarEvents", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            List<RecommendedEvent> interactions = recommendationService
                    .getInteractionsCount(request.getEventIdList());

            interactions.stream()
                    .map(mapper::toProto)
                    .forEach(responseObserver::onNext);

            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in getInteractionsCount", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
