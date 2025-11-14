package dev.devrunner.search.techblog.dto;

import dev.devrunner.elasticsearch.api.techblog.TechBlogSearchResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * TechBlog search response DTO
 */
@Schema(description = "TechBlog search response")
@Getter
@AllArgsConstructor
public class TechBlogSearchResponse {
    @Schema(description = "List of tech blog cards")
    private final List<TechBlogCard> techBlogs;

    @Schema(description = "Whether next page exists", example = "true")
    private final boolean hasNext;

    @Schema(description = "Number of results in current page", example = "30")
    private final int count;

    @Schema(description = "Total number of results matching the query", example = "150")
    private final long totalHits;

    public static TechBlogSearchResponse from(TechBlogSearchResult result) {
        List<TechBlogCard> cards = result.docs().stream()
                .map(TechBlogCard::from)
                .toList();

        return new TechBlogSearchResponse(
                cards,
                result.hasNext(),
                cards.size(),
                result.totalHits()
        );
    }
}
