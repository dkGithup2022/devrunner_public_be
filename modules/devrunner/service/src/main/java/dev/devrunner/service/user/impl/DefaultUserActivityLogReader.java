package dev.devrunner.service.user.impl;

import dev.devrunner.model.activityLog.BookmarkActivityLog;
import dev.devrunner.model.activityLog.CommentActivityLog;
import dev.devrunner.model.activityLog.LikeActivityLog;
import dev.devrunner.model.activityLog.PostActivityLog;
import dev.devrunner.model.bookmark.Bookmark;
import dev.devrunner.model.comment.CommentRead;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.communitypost.CommunityPostIdentity;
import dev.devrunner.model.communitypost.CommunityPostRead;
import dev.devrunner.model.job.Job;
import dev.devrunner.model.job.JobIdentity;
import dev.devrunner.model.reaction.Reaction;
import dev.devrunner.model.reaction.ReactionType;
import dev.devrunner.model.techblog.TechBlog;
import dev.devrunner.model.techblog.TechBlogIdentity;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.service.bookmark.BookmarkReader;
import dev.devrunner.service.comment.CommentReader;
import dev.devrunner.service.communitypost.CommunityPostReader;
import dev.devrunner.service.job.JobReader;
import dev.devrunner.service.reaction.ReactionReader;
import dev.devrunner.service.techblog.TechBlogReader;
import dev.devrunner.service.user.UserActivityLogReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 사용자 활동 로그 조회 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultUserActivityLogReader implements UserActivityLogReader {

    private final CommentReader commentReader;
    private final ReactionReader reactionReader;
    private final BookmarkReader bookmarkReader;
    private final JobReader jobReader;
    private final CommunityPostReader communityPostReader;
    private final TechBlogReader techBlogReader;

    @Override
    public List<CommentActivityLog> getCommentActivityLogs(UserIdentity userIdentity, int page, int size) {
        log.info("Fetching comment activity logs for user: {}, page: {}, size: {}",
                userIdentity.getUserId(), page, size);

        // 1. 사용자의 댓글 조회 (pagination 사용)
        List<CommentRead> comments = commentReader.getByUserId(userIdentity, page, size);

        // 2. COMMUNITY_POST 타입 필터링하여 targetId 수집
        List<Long> postIds = comments.stream()
                .filter(c -> c.getTargetType() == TargetType.COMMUNITY_POST)
                .map(CommentRead::getTargetId)
                .distinct()
                .collect(Collectors.toList());

        // 3. JOB 타입 필터링하여 targetId 수집
        List<Long> jobIds = comments.stream()
                .filter(c -> c.getTargetType() == TargetType.JOB)
                .map(CommentRead::getTargetId)
                .distinct()
                .collect(Collectors.toList());

        // 4. Bulk fetch로 모든 post 한 번에 조회 (N+1 해결)
        Map<Long, CommunityPostRead> postMap = communityPostReader.getByIds(
                postIds.stream().map(CommunityPostIdentity::new).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(CommunityPostRead::getCommunityPostId, p -> p));

        // 5. Bulk fetch로 모든 job 한 번에 조회 (N+1 해결)
        Map<Long, Job> jobMap = jobReader.getByIds(
                jobIds.stream().map(JobIdentity::new).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(Job::getJobId, j -> j));

        // 6. ActivityLog로 변환 (Map 기반, null 필터링)
        return comments.stream()
                .map(c -> toCommentActivityLog(c, postMap, jobMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<LikeActivityLog> getLikeActivityLogs(UserIdentity userIdentity, int page, int size) {
        log.info("Fetching like activity logs for user: {}, page: {}, size: {}",
                userIdentity.getUserId(), page, size);

        // 1. 사용자의 LIKE 반응 조회 (pagination 사용)
        List<Reaction> reactions = reactionReader.getByUserId(
                userIdentity.getUserId(),
                ReactionType.LIKE,
                page,
                size
        );

        // 2. COMMUNITY_POST 타입 필터링하여 targetId 수집
        List<Long> postIds = reactions.stream()
                .filter(r -> r.getTargetType() == TargetType.COMMUNITY_POST)
                .map(Reaction::getTargetId)
                .distinct()
                .collect(Collectors.toList());

        // 3. JOB 타입 필터링하여 targetId 수집
        List<Long> jobIds = reactions.stream()
                .filter(r -> r.getTargetType() == TargetType.JOB)
                .map(Reaction::getTargetId)
                .distinct()
                .collect(Collectors.toList());

        // 4. Bulk fetch로 모든 post 한 번에 조회 (N+1 해결)
        Map<Long, CommunityPostRead> postMap = communityPostReader.getByIds(
                postIds.stream().map(CommunityPostIdentity::new).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(CommunityPostRead::getCommunityPostId, p -> p));

        // 5. Bulk fetch로 모든 job 한 번에 조회 (N+1 해결)
        Map<Long, Job> jobMap = jobReader.getByIds(
                jobIds.stream().map(JobIdentity::new).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(Job::getJobId, j -> j));

        // 6. ActivityLog로 변환 (Map 기반, null 필터링)
        return reactions.stream()
                .map(r -> toLikeActivityLog(r, postMap, jobMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookmarkActivityLog> getBookmarkActivityLogs(UserIdentity userIdentity, int page, int size) {
        log.info("Fetching bookmark activity logs for user: {}, page: {}, size: {}",
                userIdentity.getUserId(), page, size);

        // 1. 사용자의 북마크 조회
        List<Bookmark> bookmarks = bookmarkReader.getByUserId(userIdentity, page, size);

        // 2. COMMUNITY_POST 타입 필터링하여 targetId 수집
        List<Long> postIds = bookmarks.stream()
                .filter(b -> b.getTargetType() == TargetType.COMMUNITY_POST)
                .map(Bookmark::getTargetId)
                .distinct()
                .collect(Collectors.toList());

        // 3. JOB 타입 필터링하여 targetId 수집
        List<Long> jobIds = bookmarks.stream()
                .filter(b -> b.getTargetType() == TargetType.JOB)
                .map(Bookmark::getTargetId)
                .distinct()
                .collect(Collectors.toList());

        // 4. TECH_BLOG 타입 필터링하여 targetId 수집
        List<Long> techBlogIds = bookmarks.stream()
                .filter(b -> b.getTargetType() == TargetType.TECH_BLOG)
                .map(Bookmark::getTargetId)
                .distinct()
                .collect(Collectors.toList());

        // 5. Bulk fetch로 모든 post 한 번에 조회 (N+1 해결)
        Map<Long, CommunityPostRead> postMap = communityPostReader.getByIds(
                postIds.stream().map(CommunityPostIdentity::new).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(CommunityPostRead::getCommunityPostId, p -> p));

        // 6. Bulk fetch로 모든 job 한 번에 조회 (N+1 해결)
        Map<Long, Job> jobMap = jobReader.getByIds(
                jobIds.stream().map(JobIdentity::new).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(Job::getJobId, j -> j));

        // 7. Bulk fetch로 모든 tech blog 한 번에 조회 (N+1 해결)
        Map<Long, TechBlog> techBlogMap = techBlogReader.getByIds(
                techBlogIds.stream().map(TechBlogIdentity::new).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(TechBlog::getTechBlogId, t -> t));

        // 8. ActivityLog로 변환 (Map 기반, null 필터링)
        return bookmarks.stream()
                .map(b -> toBookmarkActivityLog(b, postMap, jobMap, techBlogMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostActivityLog> getPostActivityLogs(UserIdentity userIdentity, int page, int size) {
        log.info("Fetching post activity logs for user: {}, page: {}, size: {}",
                userIdentity.getUserId(), page, size);

        // 1. 사용자의 글 조회
        List<CommunityPostRead> posts = communityPostReader.getByUserId(userIdentity.getUserId());

        // 2. Pagination 적용
        List<CommunityPostRead> pagedPosts = posts.stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());

        // 3. ActivityLog로 변환
        return pagedPosts.stream()
                .map(this::toPostActivityLog)
                .collect(Collectors.toList());
    }

    /**
     * CommentRead → CommentActivityLog 변환 (Map 기반)
     */
    private CommentActivityLog toCommentActivityLog(CommentRead comment, Map<Long, CommunityPostRead> postMap, Map<Long, Job> jobMap) {
        if (comment.getTargetType() == TargetType.COMMUNITY_POST) {
            CommunityPostRead post = postMap.get(comment.getTargetId());
            if (post != null) {
                Popularity popularity = post.getPopularity();

                return new CommentActivityLog(
                        comment.getCommentId(),
                        comment.getContent(),
                        comment.getIsHidden(),
                        comment.getCreatedAt(),
                        comment.getParentId(),
                        comment.getTargetType(),
                        comment.getTargetId(),
                        post.getTitle(),
                        post.getNickname(),
                        popularity.getViewCount(),
                        popularity.getLikeCount(),
                        popularity.getCommentCount()
                );
            }
        } else if (comment.getTargetType() == TargetType.JOB) {
            Job job = jobMap.get(comment.getTargetId());
            if (job != null) {
                Popularity popularity = job.getPopularity();

                return new CommentActivityLog(
                        comment.getCommentId(),
                        comment.getContent(),
                        comment.getIsHidden(),
                        comment.getCreatedAt(),
                        comment.getParentId(),
                        comment.getTargetType(),
                        comment.getTargetId(),
                        job.getTitle(),
                        "", // 임시
                        popularity.getViewCount(),
                        popularity.getLikeCount(),
                        popularity.getCommentCount()
                );
            }
        }
        return null;
    }

    /**
     * Reaction → LikeActivityLog 변환 (Map 기반)
     */
    private LikeActivityLog toLikeActivityLog(Reaction reaction, Map<Long, CommunityPostRead> postMap, Map<Long, Job> jobMap) {
        if (reaction.getTargetType() == TargetType.COMMUNITY_POST) {
            CommunityPostRead post = postMap.get(reaction.getTargetId());
            if (post != null) {
                Popularity popularity = post.getPopularity();

                return new LikeActivityLog(
                        reaction.getReactionId(),
                        reaction.getCreatedAt(),
                        reaction.getTargetType(),
                        reaction.getTargetId(),
                        post.getTitle(),
                        post.getNickname(),
                        popularity.getViewCount(),
                        popularity.getLikeCount(),
                        popularity.getCommentCount()
                );
            }
        } else if (reaction.getTargetType() == TargetType.JOB) {
            Job job = jobMap.get(reaction.getTargetId());
            if (job != null) {
                Popularity popularity = job.getPopularity();

                return new LikeActivityLog(
                        reaction.getReactionId(),
                        reaction.getCreatedAt(),
                        reaction.getTargetType(),
                        reaction.getTargetId(),
                        job.getTitle(),
                        "", // 임시
                        popularity.getViewCount(),
                        popularity.getLikeCount(),
                        popularity.getCommentCount()
                );
            }
        }
        return null;
    }

    /**
     * Bookmark → BookmarkActivityLog 변환 (Map 기반)
     */
    private BookmarkActivityLog toBookmarkActivityLog(Bookmark bookmark, Map<Long, CommunityPostRead> postMap, Map<Long, Job> jobMap, Map<Long,TechBlog> techBlogMap) {
        if (bookmark.getTargetType() == TargetType.COMMUNITY_POST) {
            CommunityPostRead post = postMap.get(bookmark.getTargetId());
            if (post != null) {
                Popularity popularity = post.getPopularity();

                return new BookmarkActivityLog(
                        bookmark.getBookmarkId(),
                        bookmark.getCreatedAt(),
                        bookmark.getTargetType(),
                        bookmark.getTargetId(),
                        post.getTitle(),
                        post.getNickname(),
                        popularity.getViewCount(),
                        popularity.getLikeCount(),
                        popularity.getCommentCount()
                );
            }
        } else if (bookmark.getTargetType() == TargetType.JOB) {
            Job job = jobMap.get(bookmark.getTargetId());
            if (job != null) {
                Popularity popularity = job.getPopularity();

                return new BookmarkActivityLog(
                        bookmark.getBookmarkId(),
                        bookmark.getCreatedAt(),
                        bookmark.getTargetType(),
                        bookmark.getTargetId(),
                        job.getTitle(),
                        "", // 임시
                        popularity.getViewCount(),
                        popularity.getLikeCount(),
                        popularity.getCommentCount()
                );
            }
        } else if (bookmark.getTargetType() == TargetType.TECH_BLOG) {
            TechBlog techBlog = techBlogMap.get(bookmark.getTargetId());
            if (techBlog != null) {
                Popularity popularity = techBlog.getPopularity();

                return new BookmarkActivityLog(
                        bookmark.getBookmarkId(),
                        bookmark.getCreatedAt(),
                        bookmark.getTargetType(),
                        bookmark.getTargetId(),
                        techBlog.getTitle(),
                        techBlog.getCompany(),
                        popularity.getViewCount(),
                        popularity.getLikeCount(),
                        popularity.getCommentCount()
                );
            }
        }
        return null;
    }

    /**
     * CommunityPostRead → PostActivityLog 변환
     */
    private PostActivityLog toPostActivityLog(CommunityPostRead post) {
        Popularity popularity = post.getPopularity();

        return new PostActivityLog(
                post.getCommunityPostId(),
                post.getCategory(),
                post.getTitle(),
                post.getMarkdownBody(),
                post.getCreatedAt(),
                popularity.getViewCount(),
                popularity.getLikeCount(),
                popularity.getCommentCount()
        );
    }
}
