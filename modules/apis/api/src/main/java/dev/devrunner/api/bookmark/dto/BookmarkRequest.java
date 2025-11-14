package dev.devrunner.api.bookmark.dto;

import dev.devrunner.model.common.TargetType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 북마크 추가/삭제 요청 DTO
 */
@Getter
@NoArgsConstructor
public class BookmarkRequest {

    @NotNull(message = "targetType is required")
    private TargetType targetType;

    @NotNull(message = "targetId is required")
    private Long targetId;
}
