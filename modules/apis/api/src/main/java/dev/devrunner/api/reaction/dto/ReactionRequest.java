package dev.devrunner.api.reaction.dto;

import dev.devrunner.model.common.TargetType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReactionRequest {
    @NotNull
    private TargetType targetType;

    @NotNull
    private Long targetId;
}
