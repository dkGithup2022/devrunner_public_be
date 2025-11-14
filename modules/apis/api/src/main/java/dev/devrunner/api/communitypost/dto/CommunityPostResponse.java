package dev.devrunner.api.communitypost.dto;

import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.communitypost.CommunityPostRead;
import dev.devrunner.model.communitypost.CommunityPostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
@Schema(description = "Community post response")
public class CommunityPostResponse {
    @Schema(description = "Community post ID", example = "1")
    private final Long communityPostId;

    @Schema(description = "User ID", example = "1")
    private final Long userId;

    @Schema(description = "User nickname", example = "testuser")
    private final String nickname;

    @Schema(description = "Category", example = "INTERVIEW_SHARE")
    private final CommunityPostCategory category;

    @Schema(description = "Title", example = "My interview experience")
    private final String title;

    @Schema(description = "Markdown body")
    private final String markdownBody;

    @Schema(description = "Company", example = "Company A")
    private final String company;

    @Schema(description = "Location", example = "Seoul")
    private final String location;

    @Schema(description = "Job ID", example = "100")
    private final Long jobId;

    @Schema(description = "Comment ID", example = "200")
    private final Long commentId;

    @Schema(description = "Popularity")
    private final Popularity popularity;

    @Schema(description = "Is deleted", example = "false")
    private final Boolean isDeleted;

    @Schema(description = "Created at", example = "2025-10-09T14:30:00Z")
    private final Instant createdAt;

    @Schema(description = "Updated at", example = "2025-10-09T14:30:00Z")
    private final Instant updatedAt;

    public static CommunityPostResponse from(CommunityPostRead communityPostRead, Long viewerUserId) {
        Long jobId = communityPostRead.getLinkedContent().getJobId() != null
                ? communityPostRead.getLinkedContent().getJobId().getJobId()
                : null;
        Long commentId = communityPostRead.getLinkedContent().getCommentId() != null
                ? communityPostRead.getLinkedContent().getCommentId().getCommentId()
                : null;

        // INTERVIEW_SHARE이고, 소유자가 아니면 Anonymous 처리
        String nickname = communityPostRead.getNickname();
        if (communityPostRead.getCategory() == CommunityPostCategory.INTERVIEW_SHARE) {
            if (viewerUserId == null || !viewerUserId.equals(communityPostRead.getUserId())) {
                nickname = "Anonymous";
            }
        }

        return new CommunityPostResponse(
                communityPostRead.getCommunityPostId(),
                communityPostRead.getUserId(),
                nickname,
                communityPostRead.getCategory(),
                communityPostRead.getTitle(),
                communityPostRead.getMarkdownBody(),
                communityPostRead.getCompany(),
                communityPostRead.getLocation(),
                jobId,
                commentId,
                communityPostRead.getPopularity(),
                communityPostRead.getIsDeleted(),
                communityPostRead.getCreatedAt(),
                communityPostRead.getUpdatedAt()
        );
    }
}
