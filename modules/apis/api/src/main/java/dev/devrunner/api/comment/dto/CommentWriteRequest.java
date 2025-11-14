package dev.devrunner.api.comment.dto;

import dev.devrunner.model.common.TargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "Comment write request")
public class CommentWriteRequest {



    @Schema(description = "Comment content", example = "Great information, thanks!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String content;

    @Schema(description = "Target type", example = "JOB", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private TargetType targetType;

    @Schema(description = "Target ID", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long targetId;

    @Schema(description = "Parent comment ID (for replies)", example = "1")
    private Long parentId;
}
