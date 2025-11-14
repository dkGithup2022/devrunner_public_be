package dev.devrunner.api.bookmark.dto;

import dev.devrunner.model.bookmark.Bookmark;
import dev.devrunner.model.common.TargetType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/**
 * 북마크 응답 DTO
 */
@Getter
@AllArgsConstructor
public class BookmarkResponse {

    private Long bookmarkId;
    private Long userId;
    private TargetType targetType;
    private Long targetId;
    private Instant createdAt;

    /**
     * Bookmark 도메인 모델을 Response DTO로 변환
     */
    public static BookmarkResponse from(Bookmark bookmark) {
        return new BookmarkResponse(
                bookmark.getBookmarkId(),
                bookmark.getUserId(),
                bookmark.getTargetType(),
                bookmark.getTargetId(),
                bookmark.getCreatedAt()
        );
    }
}
