package dev.devrunner.model.common;

import lombok.Value;

/**
 * 인기도 정보를 나타내는 Value Object
 * Job, CommunityPost, TechBlog에서 공통으로 사용
 */
@Value
public class Popularity {
    Long viewCount;
    Long commentCount;
    Long likeCount;
    Long dislikeCount;

    public static Popularity empty() {
        return new Popularity(0L, 0L, 0L, 0L);
    }

    public Popularity incrementViewCount() {
        return new Popularity(viewCount + 1, commentCount, likeCount, dislikeCount);
    }

    public Popularity incrementViewCount(long adder) {
        return new Popularity(viewCount + adder, commentCount, likeCount, dislikeCount);
    }

    public Popularity incrementCommentCount() {
        return new Popularity(viewCount, commentCount + 1, likeCount, dislikeCount);
    }

    public Popularity incrementLikeCount() {
        return new Popularity(viewCount, commentCount, likeCount + 1, dislikeCount);
    }

    public Popularity decrementLikeCount() {
        return new Popularity(viewCount, commentCount, Math.max(0, likeCount - 1), dislikeCount);
    }

    public Popularity incrementDislikeCount() {
        return new Popularity(viewCount, commentCount, likeCount, dislikeCount + 1);
    }

    public Popularity decrementDislikeCount() {
        return new Popularity(viewCount, commentCount, likeCount, Math.max(0, dislikeCount - 1));
    }
}
