package dev.devrunner.outbox.command;

import dev.devrunner.model.common.TargetType;
import dev.devrunner.outbox.model.UpdateType;
import lombok.Builder;
import lombok.Value;

/**
 * Command for querying pending outbox events
 */
@Value
@Builder
public class FindPendingEventsCommand {
    int limit;
    TargetType targetType;      // Optional: filter by target type (null = all types)
    UpdateType updateType;      // Optional: filter by update type (null = all types)

    public static FindPendingEventsCommand of(int limit) {
        return FindPendingEventsCommand.builder()
            .limit(limit)
            .build();
    }

    public static FindPendingEventsCommand ofType(int limit, TargetType targetType) {
        return FindPendingEventsCommand.builder()
            .limit(limit)
            .targetType(targetType)
            .build();
    }

    public static FindPendingEventsCommand ofUpdateType(int limit, UpdateType updateType) {
        return FindPendingEventsCommand.builder()
            .limit(limit)
            .updateType(updateType)
            .build();
    }
}
