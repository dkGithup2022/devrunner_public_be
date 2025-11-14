package dev.devrunner.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.util.List;

/**
 * 사용자 글 작성 활동 로그 목록 응답 DTO
 */
@Value
@Schema(description = "User post activity logs response")
public class UserPostActivityLogsResponse {

    @Schema(description = "List of post activity logs")
    List<PostActivityLogResponse> logs;

    @Schema(description = "Page number (0-based)", example = "0")
    Integer page;

    @Schema(description = "Page size", example = "20")
    Integer size;
}
