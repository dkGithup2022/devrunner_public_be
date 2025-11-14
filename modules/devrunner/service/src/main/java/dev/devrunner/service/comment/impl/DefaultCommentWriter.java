package dev.devrunner.service.comment.impl;

import dev.devrunner.exception.auth.UnauthorizedException;
import dev.devrunner.exception.comment.CommentNotFoundException;
import dev.devrunner.exception.comment.WrongCommentException;
import dev.devrunner.infra.comment.repository.CommentRepository;
import dev.devrunner.infra.communitypost.repository.CommunityPostRepository;
import dev.devrunner.infra.job.repository.JobRepository;
import dev.devrunner.infra.user.repository.UserRepository;
import dev.devrunner.model.comment.Comment;
import dev.devrunner.model.comment.CommentIdentity;
import dev.devrunner.model.comment.CommentRead;
import dev.devrunner.model.common.CommentOrder;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.communitypost.CommunityPostIdentity;
import dev.devrunner.model.job.JobIdentity;
import dev.devrunner.model.user.User;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.service.comment.CommentWriter;
import dev.devrunner.service.comment.dto.CommentWriteCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comment 비즈니스 로직 서비스 구현체
 * <p>
 * 계층형 댓글 구조를 CommentOrder로 관리합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultCommentWriter implements CommentWriter {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final CommunityPostRepository communityPostRepository;

    @Override
    @Transactional
    public Comment write(CommentWriteCommand command) {
        // 1. 트랜잭션 시작 직후 아티클의 comment_count 증가 (row lock 획득)
        //    → commentOrder 계산이 직렬화되어 중복 방지
        incrementArticleCommentCount(command.targetType(), command.targetId());

        Comment savedComment;

        if (command.isRootComment()) {
            savedComment = writeRootComment(command);
        } else {
            savedComment = writeReplyComment(command);
        }

        // 댓글 작성 후 작성자의 commentCount 증가
        incrementUserCommentCount(command.userId());

        return savedComment;
    }

    /**
     * 최상위 댓글 작성
     */
    private Comment writeRootComment(CommentWriteCommand command) {
        log.info("Writing new root comment for {} {}", command.targetType(), command.targetId());

        // 1. 최대 commentOrder 조회
        var maxCommentOrder = commentRepository.findMaxCommentOrder(
                command.targetType(),
                command.targetId()
        );
        var newCommentOrder = (maxCommentOrder == null ? 0 : maxCommentOrder) + 1;

        // 2. 최상위 댓글 생성 (level: 0, sortNumber: 0)
        var comment = Comment.newComment(
                command.userId(),
                command.content(),
                command.targetType(),
                command.targetId()
        );
        var order = CommentOrder.newRootComment(newCommentOrder);
        var commentWithOrder = comment.updateCommentOrder(order);

        // 3. 저장
        Comment saved = commentRepository.save(commentWithOrder);
        log.info("Root comment created: commentId={}, commentOrder={}", saved.getCommentId(), newCommentOrder);

        return saved;
    }

    /**
     * 대댓글 작성
     */
    private Comment writeReplyComment(CommentWriteCommand command) {
        log.info("Writing reply to comment {} for {} {}",
                command.parentId(), command.targetType(), command.targetId());

        // 1. 부모 댓글 조회
        CommentRead parent = commentRepository.findById(new CommentIdentity(command.parentId()))
                .orElseThrow(() -> new CommentNotFoundException("Comment with id " + command.parentId() + " not found"));

        CommentOrder parentOrder = parent.getCommentOrder();

        // 2. 새 대댓글의 sortNumber 계산
        // 부모의 sortNumber + 부모의 childCount + 1
        Integer newSortNumber = parentOrder.getSortNumber() + parentOrder.getChildCount() + 1;
        Integer newLevel = parentOrder.getLevel() + 1;

        // 3. 기존 댓글들의 sortNumber를 +1씩 증가 (newSortNumber 이상인 것들)
        commentRepository.incrementSortNumbersAbove(
                command.targetType(),
                command.targetId(),
                parentOrder.getCommentOrder(),
                newSortNumber - 1  // newSortNumber보다 큰 값들을 증가
        );

        // 4. 대댓글 생성
        Comment reply = Comment.newReply(
                command.userId(),
                command.content(),
                command.targetType(),
                command.targetId(),
                command.parentId()
        );

        CommentOrder replyOrder = CommentOrder.newReply(
                parentOrder.getCommentOrder(),
                newLevel,
                newSortNumber,
                command.parentId()
        );

        Comment replyWithOrder = reply.updateCommentOrder(replyOrder);

        // 5. 저장
        Comment saved = commentRepository.save(replyWithOrder);

        // 6. 부모 댓글의 childCount를 재귀적으로 +1 (모든 조상 댓글)
        incrementAllParentsChildCount(command.parentId());

        log.info("Reply created: commentId={}, parentId={}, level={}, sortNumber={}",
                saved.getCommentId(), command.parentId(), newLevel, newSortNumber);

        return saved;
    }

    @Override
    @Transactional
    public Comment updateComment(UserIdentity requestUser, Long commentId, String newContent) {
        log.info("Updating comment {} / requestUserID : {} ", commentId, requestUser.getUserId());


        CommentRead commentRead = commentRepository.findById(new CommentIdentity(commentId))
                .orElseThrow(() -> new CommentNotFoundException("commentId=" + commentId + " not found"));

        if(!commentRead.getUserId().equals(requestUser.getUserId()))
            throw new UnauthorizedException("You are not authorized to update this comment");

        // CommentRead → Comment conversion for update
        Comment comment = toComment(commentRead);
        Comment updated = comment.updateContent(newContent);
        return commentRepository.save(updated);
    }

    @Override
    @Transactional
    public Comment hideComment(UserIdentity requestUser, Long commentId) {
        log.info("Hiding comment {} / userId : {}", commentId, requestUser.getUserId());

        CommentRead commentRead = commentRepository.findById(new CommentIdentity(commentId))
                .orElseThrow(() -> new CommentNotFoundException("commentId=" + commentId + " not found"));

        if(!commentRead.getUserId().equals(requestUser.getUserId()))
            throw new UnauthorizedException("You are not authorized to update this comment");

        if (commentRead.getIsHidden())
            throw new WrongCommentException("already hidden comment");

        Comment comment = toComment(commentRead);
        Comment hidden = comment.hide();
        return commentRepository.save(hidden);
    }

    @Override
    @Transactional
    public Comment showComment(UserIdentity requestUser, Long commentId) {
        log.info("Showing comment {} - requestUserId :{}", commentId, requestUser.getUserId());

        CommentRead commentRead = commentRepository.findById(new CommentIdentity(commentId))
                .orElseThrow(() -> new CommentNotFoundException("commentId=" + commentId + " not found"));

        if (!commentRead.getIsHidden())
            throw new WrongCommentException("already shown comment");

        if(!commentRead.getUserId().equals(requestUser.getUserId()))
            throw new UnauthorizedException("You are not authorized to update this comment");

        Comment comment = toComment(commentRead);
        Comment shown = comment.show();
        return commentRepository.save(shown);
    }

    /**
     * 부모 댓글을 재귀적으로 타고 올라가며 childCount를 +1 증가
     * (성능 이슈 인지하고 있으나, 현재 스펙 유지)
     */
    private void incrementAllParentsChildCount(Long parentId) {
        if (parentId == null) {
            return;
        }

        CommentRead parent = commentRepository.findById(new CommentIdentity(parentId))
                .orElse(null);

        if (parent == null) {
            return;
        }

        // childCount 증가
        commentRepository.incrementChildCount(parentId);

        // 재귀: 부모의 부모로 계속 올라감
        incrementAllParentsChildCount(parent.getParentId());
    }

    /**
     * User의 commentCount 증가
     */
    private void incrementUserCommentCount(Long userId) {
        UserIdentity userIdentity = new UserIdentity(userId);
        User user = userRepository.findById(userIdentity)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        User updatedUser = user.incrementCommentCount();
        userRepository.save(updatedUser);
        log.info("User {} commentCount incremented", userId);
    }

    /**
     * 아티클(Job, CommunityPost)의 comment_count 증가
     *
     * 트랜잭션 시작 직후 호출하여 해당 아티클 row에 대한 lock을 획득.
     * 이를 통해 동시에 여러 댓글이 작성되어도 commentOrder 계산이 직렬화됨.
     */
    private void incrementArticleCommentCount(TargetType targetType, Long targetId) {
        switch (targetType) {
            case JOB:
                jobRepository.increaseCommentCount(new JobIdentity(targetId));
                log.info("Job {} comment_count incremented (row locked)", targetId);
                break;
            case COMMUNITY_POST:
                communityPostRepository.increaseCommentCount(new CommunityPostIdentity(targetId));
                log.info("CommunityPost {} comment_count incremented (row locked)", targetId);
                break;
            case TECH_BLOG:
                // TechBlog는 댓글 기능 없음 - skip
                break;
            default:
                log.warn("Unknown target type: {}", targetType);
        }
    }

    /**
     * CommentRead → Comment conversion (for write operations)
     * Nickname is ignored as it's not needed for write operations.
     */
    private Comment toComment(CommentRead commentRead) {
        return new Comment(
                commentRead.getCommentId(),
                commentRead.getUserId(),
                commentRead.getContent(),
                commentRead.getTargetType(),
                commentRead.getTargetId(),
                commentRead.getParentId(),
                commentRead.getCommentOrder(),
                commentRead.getIsHidden(),
                commentRead.getCreatedAt(),
                commentRead.getUpdatedAt()
        );
    }
}
