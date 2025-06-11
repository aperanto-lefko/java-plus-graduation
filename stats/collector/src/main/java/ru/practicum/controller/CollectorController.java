package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class CollectorController extends UserActionControllerGrpc.UserActionControllerImplBase {
}
