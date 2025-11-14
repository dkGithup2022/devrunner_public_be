package dev.devrunner.search.communitypost.dto;

import dev.devrunner.elasticsearch.document.CommunityPostDoc;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

import static dev.devrunner.elasticsearch.util.InstantConverter.fromEpochMillis;

/**
 * CommunityPost search result card DTO
 *
 * Contains only essential information to display in search result list
 */
@Schema(description = "CommunityPost search result card")
public record CommunityPostCard(
    @Schema(description = "CommunityPost ID", example = "1")
    Long communityPostId,

    @Schema(description = "User ID", example = "123")
    Long userId,

    @Schema(description = "User nickname", example = "개발자123")
    String nickname,

    @Schema(description = "Category", example = "INTERVIEW_SHARE")
    String category,

    @Schema(description = "Post title", example = "Google 면접 후기")
    String title,

    @Schema(description = "Markdown body", example = "# 면접 경험 공유...")
    String markdownBody,

    @Schema(description = "Company name", example = "Google")
    String company,

    @Schema(description = "Location", example = "Seoul")
    String location,

    @Schema(description = "Linked job ID", example = "456")
    Long linkedJobId,

    @Schema(description = "Posted from job comment", example = "true")
    Boolean isFromJobComment,

    @Schema(description = "View count", example = "1234")
    Long viewCount,

    @Schema(description = "Comment count", example = "56")
    Long commentCount,

    @Schema(description = "Like count", example = "89")
    Long likeCount,

    @Schema(description = "Created timestamp")
    Instant createdAt,

    @Schema(description = "Last updated timestamp")
    Instant updatedAt
) {
    /**
     * Create CommunityPostCard from CommunityPostDoc with nickname
     */
    public static CommunityPostCard from(CommunityPostDoc doc, String nickname) {
        return new CommunityPostCard(
            doc.getCommunityPostId(),
            doc.getUserId(),
            nickname,
            doc.getCategory(),
            doc.getTitle(),
            doc.getMarkdownBody(),
            doc.getCompany(),
            doc.getLocation(),
            doc.getLinkedJobId(),
            doc.getIsFromJobComment(),
            doc.getPopularityViewCount(),
            doc.getPopularityCommentCount(),
            doc.getPopularityLikeCount(),
                fromEpochMillis(doc.getCreatedAt()),
                fromEpochMillis(doc.getUpdatedAt())
        );
    }
}
