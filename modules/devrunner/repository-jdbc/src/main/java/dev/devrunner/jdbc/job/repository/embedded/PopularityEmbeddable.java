package dev.devrunner.jdbc.job.repository.embedded;

import dev.devrunner.model.common.Popularity;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Popularity의 Entity 레이어 표현
 * Spring Data JDBC의 @Embedded로 사용되며, jobs 테이블의 컬럼으로 flatten됩니다.
 */
@Getter
@AllArgsConstructor
public class PopularityEmbeddable {
    Long viewCount;
    Long commentCount;
    Long likeCount;
    Long dislikeCount;

    public static PopularityEmbeddable from(Popularity domain) {
        if (domain == null) {
            return null;
        }
        return new PopularityEmbeddable(
            domain.getViewCount(),
            domain.getCommentCount(),
            domain.getLikeCount(),
            domain.getDislikeCount()
        );
    }

    public Popularity toDomain() {
        return new Popularity(
            viewCount,
            commentCount,
            likeCount,
            dislikeCount
        );
    }
}
