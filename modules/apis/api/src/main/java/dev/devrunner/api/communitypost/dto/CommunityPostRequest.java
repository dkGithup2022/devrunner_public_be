package dev.devrunner.api.communitypost.dto;

import dev.devrunner.model.comment.CommentIdentity;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.communitypost.CommunityPost;
import dev.devrunner.model.communitypost.CommunityPostCategory;
import dev.devrunner.model.communitypost.LinkedContent;
import dev.devrunner.model.job.JobIdentity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPostRequest {
    private Long communityPostId;

    @NotNull
    private CommunityPostCategory category;

    @NotBlank
    private String title;

    @NotBlank
    private String markdownBody;

    private String company;
    private String location;
    private Long jobId;
    private Long commentId;

    public CommunityPost toCommunityPost(Long requestUserId) {
        Instant now = Instant.now();
        LinkedContent linkedContent = createLinkedContent(jobId, commentId);

        return new CommunityPost(
                communityPostId,
                requestUserId,
                category,
                title,
                markdownBody,
                company,
                location,
                linkedContent,
                Popularity.empty(),
                false,
                now,
                now
        );
    }

    private LinkedContent createLinkedContent(Long jobId, Long commentId) {
        if (jobId == null && commentId == null) {
            return LinkedContent.none();
        }
        if (commentId != null) {
            return LinkedContent.fromJobComment(jobId, commentId);
        }
        return LinkedContent.fromJob(jobId);
    }
}
