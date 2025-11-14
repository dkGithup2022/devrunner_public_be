package dev.devrunner.search.techblog.dto;

import dev.devrunner.elasticsearch.document.TechBlogDoc;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

import static dev.devrunner.elasticsearch.util.InstantConverter.fromEpochMillis;

/**
 * TechBlog search result card DTO
 * <p>
 * Contains only essential information to display in search result list
 */
@Schema(description = "TechBlog search result card")
public record TechBlogCard(
        @Schema(description = "TechBlog ID", example = "1")
        Long techBlogId,

        @Schema(description = "TechBlog URL", example = "https://engineering.fb.com/2024/01/01/post")
        String url,

        @Schema(description = "Company name", example = "Meta")
        String company,

        @Schema(description = "Blog title", example = "Building Scalable Systems at Meta")
        String title,

        @Schema(description = "One-line summary", example = "Learn how we built our distributed system")
        String oneLiner,

        @Schema(description = "Summary", example = "This article discusses...")
        String summary,

        @Schema(description = "Thumbnail image URL", example = "https://example.com/thumb.jpg")
        String thumbnailUrl,

        @Schema(description = "Tech categories", example = "[\"Distributed Systems\", \"Java\"]")
        List<String> techCategories,

        @Schema(description = "Original blog post URL", example = "https://engineering.fb.com/original")
        String originalUrl,

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
     * Create TechBlogCard from TechBlogDoc
     */
    public static TechBlogCard from(TechBlogDoc doc) {
        return new TechBlogCard(
                doc.getTechBlogId(),
                doc.getUrl(),
                doc.getCompany(),
                doc.getTitle(),
                doc.getOneLiner(),
                doc.getSummary(),
                doc.getThumbnailUrl(),
                doc.getTechCategories(),
                doc.getOriginalUrl(),
                doc.getPopularityViewCount(),
                doc.getPopularityCommentCount(),
                doc.getPopularityLikeCount(),
                fromEpochMillis(doc.getCreatedAt()),
                fromEpochMillis(doc.getUpdatedAt())
        );
    }
}
