package dev.devrunner.jdbc.user.repository.embedded;

import dev.devrunner.model.user.UserMetrics;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * UserMetrics의 Entity 레이어 표현
 * Spring Data JDBC의 @Embedded로 사용되며, users 테이블의 컬럼으로 flatten됩니다.
 */
@Getter
@AllArgsConstructor
public class UserMetricsEmbeddable {
    Long postCount;
    Long commentCount;
    Long likesReceived;
    Long likeGivenCount;
    Long bookmarkCount;

    public static UserMetricsEmbeddable from(UserMetrics domain) {
        if (domain == null) {
            return null;
        }
        return new UserMetricsEmbeddable(
            domain.getPostCount(),
            domain.getCommentCount(),
            domain.getLikesReceived(),
            domain.getLikeGivenCount(),
            domain.getBookmarkCount()
        );
    }

    public UserMetrics toDomain() {
        return new UserMetrics(
            postCount,
            commentCount,
            likesReceived,
            likeGivenCount,
            bookmarkCount
        );
    }
}
