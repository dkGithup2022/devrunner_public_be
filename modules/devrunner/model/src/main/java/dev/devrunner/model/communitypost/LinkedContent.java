package dev.devrunner.model.communitypost;

import dev.devrunner.model.comment.CommentIdentity;
import dev.devrunner.model.job.JobIdentity;
import lombok.Value;

/**
 * LinkedContent - CommunityPost의 연결 정보
 *
 * Job 또는 Comment와 연결된 게시글의 출처 정보를 관리
 */
@Value
public class LinkedContent {
    JobIdentity jobId;
    CommentIdentity commentId;
    Boolean isFromJobComment;

    public static LinkedContent none() {
        return new LinkedContent(null, null, false);
    }

    public static LinkedContent fromJob(Long jobId) {
        return new LinkedContent(new JobIdentity(jobId), null, true);
    }

    public static LinkedContent fromJobComment(Long jobId, Long commentId) {
        return new LinkedContent(
            new JobIdentity(jobId),
            new CommentIdentity(commentId),
            true
        );
    }
}
