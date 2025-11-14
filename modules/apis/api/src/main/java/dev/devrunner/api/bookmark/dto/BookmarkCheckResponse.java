package dev.devrunner.api.bookmark.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 북마크 여부 확인 응답 DTO
 */
@Getter
@AllArgsConstructor
public class BookmarkCheckResponse {

    @JsonProperty("isBookmarked")
    private boolean bookmarked;
    private Long bookmarkId; // null if not bookmarked

    /**
     * 북마크되지 않은 경우
     */
    public static BookmarkCheckResponse notBookmarked() {
        return new BookmarkCheckResponse(false, null);
    }

    /**
     * 북마크된 경우
     */
    public static BookmarkCheckResponse bookmarked(Long bookmarkId) {
        return new BookmarkCheckResponse(true, bookmarkId);
    }
}
