package dev.devrunner.model.communitypost;

import dev.devrunner.model.AuditProps;
import dev.devrunner.model.common.Popularity;
import lombok.Value;
import java.time.Instant;

/**
 * CommunityPostRead - Query model for reading community posts with user information
 *
 * This model includes user nickname for API responses.
 * Use CommunityPost model for write operations (create, update).
 */
@Value
public class CommunityPostRead implements AuditProps {
    Long communityPostId;
    Long userId;
    String nickname;  // User nickname - fetched via JOIN
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
}
