package dev.devrunner.model.communitypost;

import dev.devrunner.model.AuditProps;
import dev.devrunner.model.common.Popularity;
import lombok.Value;
import java.time.Instant;

@Value
public class CommunityPost implements AuditProps {
    Long communityPostId;
    Long userId;
    CommunityPostCategory category;
    String title;
    String markdownBody;
    String company;
    String location;
    LinkedContent linkedContent;
    Popularity popularity;
    Boolean isDeleted;
    Instant createdAt;
    Instant updatedAt;

    public static CommunityPost newPost(
        Long userId,
        CommunityPostCategory category,
        String title,
        String markdownBody
    ) {
        Instant now = Instant.now();
        return new CommunityPost(
            null,
            userId,
            category,
            title,
            markdownBody,
            null,
            null,
            LinkedContent.none(),
            Popularity.empty(),
            false,
            now,
            now
        );
    }

    public static CommunityPost fromJobComment(
        Long userId,
        String title,
        String markdownBody,
        Long jobId,
        Long commentId,
        String company,
        String location
    ) {
        Instant now = Instant.now();
        return new CommunityPost(
            null,
            userId,
            CommunityPostCategory.INTERVIEW_SHARE,
            title,
            markdownBody,
            company,
            location,
            LinkedContent.fromJobComment(jobId, commentId),
            Popularity.empty(),
            false,
            now,
            now
        );
    }

    public CommunityPost incrementViewCount() {
        return new CommunityPost(
            communityPostId, userId, category, title, markdownBody,
            company, location, linkedContent,
            popularity.incrementViewCount(), isDeleted, createdAt, Instant.now()
        );
    }
    public CommunityPost incrementViewCount(long adder) {
        return new CommunityPost(
                communityPostId, userId, category, title, markdownBody,
                company, location, linkedContent,
                popularity.incrementViewCount(adder), isDeleted, createdAt, Instant.now()
        );
    }

    public CommunityPost incrementCommentCount() {
        return new CommunityPost(
            communityPostId, userId, category, title, markdownBody,
            company, location, linkedContent,
            popularity.incrementCommentCount(), isDeleted, createdAt, Instant.now()
        );
    }

    public CommunityPost incrementLikeCount() {
        return new CommunityPost(
            communityPostId, userId, category, title, markdownBody,
            company, location, linkedContent,
            popularity.incrementLikeCount(), isDeleted, createdAt, Instant.now()
        );
    }

    public CommunityPost decrementLikeCount() {
        return new CommunityPost(
            communityPostId, userId, category, title, markdownBody,
            company, location, linkedContent,
            popularity.decrementLikeCount(), isDeleted, createdAt, Instant.now()
        );
    }

    public CommunityPost markAsDeleted() {
        return new CommunityPost(
            communityPostId, userId, category, title, markdownBody,
            company, location, linkedContent,
            popularity, true, createdAt, Instant.now()
        );
    }
}
