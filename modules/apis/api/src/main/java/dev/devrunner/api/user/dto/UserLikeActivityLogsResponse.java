package dev.devrunner.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.util.List;

/**
 * 사용자 좋아요 활동 로그 목록 응답 DTO
 */
@Value
@Schema(description = "User like activity logs response")
public class UserLikeActivityLogsResponse {

    @Schema(description = "List of like activity logs")
    List<LikeActivityLogResponse> logs;

    @Schema(description = "Page number (0-based)", example = "0")
    Integer page;

    @Schema(description = "Page size", example = "20")
    Integer size;
}
