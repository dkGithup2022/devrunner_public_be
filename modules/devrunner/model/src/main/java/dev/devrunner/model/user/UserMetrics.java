package dev.devrunner.model.user;

import lombok.Value;

/**
 * 사용자 활동 통계 Value Object
 *
 * User의 활동 관련 메트릭을 관리하는 불변 객체입니다.
 * Spring Data JDBC에서 Embedded 방식으로 users 테이블에 flatten되어 저장됩니다.
 */
@Value
public class UserMetrics {
    Long postCount;
    Long commentCount;
    Long likesReceived;
    Long likeGivenCount;
    Long bookmarkCount;

    /**
     * 초기 메트릭 생성 (모든 카운트 0)
     */
    public static UserMetrics initial() {
        return new UserMetrics(0L, 0L, 0L, 0L, 0L);
    }

    /**
     * 작성 글 수 증가
     */
    public UserMetrics incrementPostCount() {
        return new UserMetrics(
            postCount + 1,
            commentCount,
            likesReceived,
            likeGivenCount,
            bookmarkCount
        );
    }

    /**
     * 작성 댓글 수 증가
     */
    public UserMetrics incrementCommentCount() {
        return new UserMetrics(
            postCount,
            commentCount + 1,
            likesReceived,
            likeGivenCount,
            bookmarkCount
        );
    }

    /**
     * 받은 좋아요 수 증가
     */
    public UserMetrics incrementLikesReceived() {
        return new UserMetrics(
            postCount,
            commentCount,
            likesReceived + 1,
            likeGivenCount,
            bookmarkCount
        );
    }

    /**
     * 받은 좋아요 수 감소
     */
    public UserMetrics decrementLikesReceived() {
        return new UserMetrics(
            postCount,
            commentCount,
            likesReceived - 1,
            likeGivenCount,
            bookmarkCount
        );
    }

    /**
     * 내가 준 좋아요 수 증가
     */
    public UserMetrics incrementLikeGivenCount() {
        return new UserMetrics(
            postCount,
            commentCount,
            likesReceived,
            likeGivenCount + 1,
            bookmarkCount
        );
    }

    /**
     * 내가 준 좋아요 수 감소
     */
    public UserMetrics decrementLikeGivenCount() {
        return new UserMetrics(
            postCount,
            commentCount,
            likesReceived,
            likeGivenCount - 1,
            bookmarkCount
        );
    }

    /**
     * 북마크 수 증가
     */
    public UserMetrics incrementBookmarkCount() {
        return new UserMetrics(
            postCount,
            commentCount,
            likesReceived,
            likeGivenCount,
            bookmarkCount + 1
        );
    }

    /**
     * 북마크 수 감소
     */
    public UserMetrics decrementBookmarkCount() {
        return new UserMetrics(
            postCount,
            commentCount,
            likesReceived,
            likeGivenCount,
            bookmarkCount - 1
        );
    }
}
