package dev.devrunner.api.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "Comment update request")
public class CommentUpdateRequest {

    @Schema(description = "Updated comment content", example = "This is updated comment content.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String content;
}
