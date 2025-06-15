package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.model.ActionType;
import ru.practicum.model.UserAction;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface UserActionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "actionType", source = "actionType", qualifiedByName = "actionTypeAvroToActionType")
    @Mapping(target = "timestamp", source = "timestamp", qualifiedByName = "timestampMsToInstant")
    UserAction toEntity(UserActionAvro avro);

    @Named("actionTypeAvroToActionType")
    default ActionType actionTypeAvroToActionType(ActionTypeAvro actionTypeAvro) {
        if (actionTypeAvro == null) {
            return null;
        }
        return ActionType.valueOf(actionTypeAvro.name());
    }
    @Named("timestampMsToInstant")
    default Instant timestampMsToInstant(long timestampMs) {
        return Instant.ofEpochMilli(timestampMs);
    }
}
