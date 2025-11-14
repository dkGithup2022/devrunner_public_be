package dev.devrunner.service.bookmark;

import dev.devrunner.exception.bookmark.BookmarkNotFoundException;
import dev.devrunner.exception.bookmark.DuplicateBookmarkException;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.user.UserIdentity;

/**
 * Bookmark 서비스 인터페이스
 *
 * 북마크 추가/삭제를 처리하는 비즈니스 로직을 제공합니다.
 */
public interface BookmarkWriter {

    /**
     * 북마크 추가
     *
     * @param userIdentity 사용자 식별자
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     * @throws DuplicateBookmarkException 이미 북마크된 경우 (409 Conflict)
     */
    void addBookmark(UserIdentity userIdentity, TargetType targetType, Long targetId);

    /**
     * 북마크 삭제
     *
     * @param userIdentity 사용자 식별자
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     * @throws BookmarkNotFoundException 북마크를 찾을 수 없는 경우 (404 Not Found)
     */
    void removeBookmark(UserIdentity userIdentity, TargetType targetType, Long targetId);
}
