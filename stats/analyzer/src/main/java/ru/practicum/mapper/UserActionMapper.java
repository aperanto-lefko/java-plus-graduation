package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.model.ActionType;
import ru.practicum.model.UserAction;


@Mapper(componentModel = "spring")
public interface UserActionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "actionType", source = "actionType", qualifiedByName = "actionTypeAvroToActionType")
    @Mapping(target = "timestamp", source = "timestamp")
    UserAction toEntity(UserActionAvro avro);

    @Named("actionTypeAvroToActionType")
    default ActionType actionTypeAvroToActionType(ActionTypeAvro actionTypeAvro) {
        if (actionTypeAvro == null) {
            return null;
        }
        return ActionType.valueOf(actionTypeAvro.name());
    }

}
