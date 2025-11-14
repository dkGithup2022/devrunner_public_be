package dev.devrunner.service.user;

import dev.devrunner.model.activityLog.BookmarkActivityLog;
import dev.devrunner.model.activityLog.CommentActivityLog;
import dev.devrunner.model.activityLog.LikeActivityLog;
import dev.devrunner.model.activityLog.PostActivityLog;
import dev.devrunner.model.user.UserIdentity;

import java.util.List;

/**
 * 사용자 활동 로그 조회 서비스 인터페이스
 *
 * 사용자의 댓글/좋아요/북마크/글 작성 활동 이력을 조회합니다.
 * 각 활동에 연관된 아티클 정보와 인기지표를 함께 제공합니다.
 */
public interface UserActivityLogReader {

    /**
     * 댓글 활동 로그 조회
     *
     * @param userIdentity 사용자 식별자
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @return 댓글 활동 로그 목록
     */
    List<CommentActivityLog> getCommentActivityLogs(UserIdentity userIdentity, int page, int size);

    /**
     * 좋아요 활동 로그 조회
     *
     * @param userIdentity 사용자 식별자
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @return 좋아요 활동 로그 목록
     */
    List<LikeActivityLog> getLikeActivityLogs(UserIdentity userIdentity, int page, int size);

    /**
     * 북마크 활동 로그 조회
     *
     * @param userIdentity 사용자 식별자
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @return 북마크 활동 로그 목록
     */
    List<BookmarkActivityLog> getBookmarkActivityLogs(UserIdentity userIdentity, int page, int size);

    /**
     * 글 작성 활동 로그 조회
     *
     * @param userIdentity 사용자 식별자
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @return 글 작성 활동 로그 목록
     */
    List<PostActivityLog> getPostActivityLogs(UserIdentity userIdentity, int page, int size);
}
