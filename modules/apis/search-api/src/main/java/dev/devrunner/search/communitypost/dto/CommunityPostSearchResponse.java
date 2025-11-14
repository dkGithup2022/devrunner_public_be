package dev.devrunner.search.communitypost.dto;

import dev.devrunner.elasticsearch.api.communitypost.CommunityPostSearchResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * CommunityPost search response DTO
 */
@Schema(description = "CommunityPost search response")
@Getter
@AllArgsConstructor
public class CommunityPostSearchResponse {
    @Schema(description = "List of community post cards")
    private final List<CommunityPostCard> communityPosts;

    @Schema(description = "Whether next page exists", example = "true")
    private final boolean hasNext;

    @Schema(description = "Number of results in current page", example = "30")
    private final int count;

    @Schema(description = "Total number of results matching the query", example = "150")
    private final long totalHits;

    public static CommunityPostSearchResponse from(
            CommunityPostSearchResult result,
            java.util.Map<Long, String> userIdToNickname
    ) {
        List<CommunityPostCard> cards = result.docs().stream()
                .map(doc ->
                        {
                            // TODO : community category enum 으로 받게 변경 .
                            var nickname =
                                    "INTERVIEW_SHARE".equals(doc.getCategory()) ? "Anonymous" : userIdToNickname.getOrDefault(doc.getUserId(), "Unknown");
                            return CommunityPostCard.from(
                                    doc, nickname);
                        }
                ).toList();

        return new CommunityPostSearchResponse(
                cards,
                result.hasNext(),
                cards.size(),
                result.totalHits()
        );
    }
}
