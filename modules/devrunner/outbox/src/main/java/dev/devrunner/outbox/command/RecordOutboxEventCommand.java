package dev.devrunner.outbox.command;

import dev.devrunner.model.common.TargetType;
import dev.devrunner.outbox.model.UpdateType;
import lombok.Value;

/**
 * Command for recording a new outbox event
 */
@Value
public class RecordOutboxEventCommand {
    TargetType targetType;
    Long targetId;
    UpdateType updateType;

    public static RecordOutboxEventCommand of(TargetType targetType, Long targetId, UpdateType updateType) {
        return new RecordOutboxEventCommand(targetType, targetId, updateType);
    }

    public static RecordOutboxEventCommand created(TargetType targetType, Long targetId) {
        return new RecordOutboxEventCommand(targetType, targetId, UpdateType.CREATED);
    }

    public static RecordOutboxEventCommand updated(TargetType targetType, Long targetId) {
        return new RecordOutboxEventCommand(targetType, targetId, UpdateType.UPDATED);
    }

    public static RecordOutboxEventCommand popularityOnly(TargetType targetType, Long targetId) {
        return new RecordOutboxEventCommand(targetType, targetId, UpdateType.POPULARITY_ONLY);
    }

    public static RecordOutboxEventCommand deleted(TargetType targetType, Long targetId) {
        return new RecordOutboxEventCommand(targetType, targetId, UpdateType.DELETED);
    }
}
