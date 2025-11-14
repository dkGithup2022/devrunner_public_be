package dev.devrunner.api.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.time.Instant;

/**
 * 사용자 활동 통계 응답 DTO
 */
@Value
@Schema(description = "User activity metrics response")
public class UserMetricsResponse {

    @Schema(description = "User ID", example = "1")
    Long userId;

    @Schema(description = "User nickname", example = "testuser")
    String nickname;

    @Schema(description = "Number of community posts written", example = "15")
    Long communityPostCount;

    @Schema(description = "Number of comments written", example = "48")
    Long commentCount;

    @Schema(description = "Number of likes given by user", example = "120")
    Long likeGivenCount;

    @Schema(description = "Number of likes received by user", example = "85")
    Long likeReceivedCount;

    @Schema(description = "Number of bookmarks", example = "32")
    Long bookmarkCount;

    @Schema(description = "User join date", example = "2025-01-01T00:00:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant joinedAt;
}
