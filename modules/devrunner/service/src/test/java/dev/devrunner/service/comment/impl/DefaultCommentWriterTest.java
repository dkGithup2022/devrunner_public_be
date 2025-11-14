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
import dev.devrunner.service.comment.dto.CommentWriteCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCommentWriterTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CommunityPostRepository communityPostRepository;

    @InjectMocks
    private DefaultCommentWriter commentWriter;

    // Sample Comment for save operations (write)
    private static final Comment sampleComment = new Comment(
            1L,                          // commentId
            1L,                          // userId
            "test content",              // content
            TargetType.JOB,              // targetType
            1L,                          // targetId
            null,                        // parentId
            CommentOrder.empty(),        // commentOrder
            false,                       // isHidden
            Instant.now(),               // createdAt
            Instant.now()                // updatedAt
    );

    // Sample CommentRead for read operations (findById)
    private static final CommentRead sampleCommentRead = new CommentRead(
            1L,                          // commentId
            1L,                          // userId
            "testuser",                  // nickname
            "test content",              // content
            TargetType.JOB,              // targetType
            1L,                          // targetId
            null,                        // parentId
            CommentOrder.empty(),        // commentOrder
            false,                       // isHidden
            Instant.now(),               // createdAt
            Instant.now()                // updatedAt
    );

    // ========== write (최상위 댓글) 테스트 ==========

    @Test
    void write_newRootComment_callsRepositorySaveAndIncrementsUserCommentCount() {
        // given
        CommentWriteCommand command = new CommentWriteCommand(
                1L,              // userId
                "test content",  // content
                TargetType.JOB,  // targetType
                1L,              // targetId
                null             // parentId (최상위 댓글)
        );

        User mockUser = User.newUser("google123", "test@example.com", "testuser");
        User updatedUser = mockUser.incrementCommentCount();

        doNothing().when(jobRepository).increaseCommentCount(any(JobIdentity.class));
        when(commentRepository.findMaxCommentOrder(any(), any())).thenReturn(null);
        when(commentRepository.save(any(Comment.class)))
                .thenReturn(sampleComment);
        when(userRepository.findById(any(UserIdentity.class)))
                .thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class)))
                .thenReturn(updatedUser);

        // when
        Comment result = commentWriter.write(command);

        // then
        assertNotNull(result);
        assertEquals(sampleComment.getContent(), result.getContent());
        verify(jobRepository).increaseCommentCount(new JobIdentity(1L));
        verify(commentRepository).save(any(Comment.class));
        verify(userRepository).findById(new UserIdentity(1L));
        verify(userRepository).save(any(User.class));
    }

    // ========== write (대댓글) 테스트 ==========

    @Test
    void write_newReply_callsRepositorySaveAndIncrementsUserCommentCount() {
        // given
        CommentWriteCommand command = new CommentWriteCommand(
                1L,              // userId
                "reply content", // content
                TargetType.JOB,  // targetType
                1L,              // targetId
                1L               // parentId (대댓글)
        );
        Comment replyComment = new Comment(
                2L, 1L, "reply content", TargetType.JOB, 1L, 1L,
                CommentOrder.empty(), false, Instant.now(), Instant.now()
        );

        User mockUser = User.newUser("google123", "test@example.com", "testuser");
        User updatedUser = mockUser.incrementCommentCount();

        // 부모 댓글 조회 mock 설정
        doNothing().when(jobRepository).increaseCommentCount(any(JobIdentity.class));
        doNothing().when(commentRepository).incrementSortNumbersAbove(any(), any(), any(), any());
        doNothing().when(commentRepository).incrementChildCount(any());
        when(commentRepository.findById(new CommentIdentity(1L)))
                .thenReturn(Optional.of(sampleCommentRead));
        when(commentRepository.save(any(Comment.class)))
                .thenReturn(replyComment);
        when(userRepository.findById(any(UserIdentity.class)))
                .thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class)))
                .thenReturn(updatedUser);

        // when
        Comment result = commentWriter.write(command);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getParentId());
        verify(jobRepository).increaseCommentCount(new JobIdentity(1L));
        verify(commentRepository).incrementSortNumbersAbove(any(), any(), any(), any());
        verify(commentRepository).incrementChildCount(1L);
        // findById는 2번 호출됨: writeReplyComment에서 1번 + incrementAllParentsChildCount에서 1번
        verify(commentRepository, times(2)).findById(new CommentIdentity(1L));
        verify(commentRepository).save(any(Comment.class));
        verify(userRepository).findById(new UserIdentity(1L));
        verify(userRepository).save(any(User.class));
    }

    // ========== updateComment 테스트 ==========

    @Test
    void updateComment_existingComment_updatesAndSaves() {
        // given
        Long commentId = 1L;
        Long userId = 1L;
        String newContent = "updated content";
        Comment updatedComment = new Comment(
                1L, 1L, newContent, TargetType.JOB, 1L, null,
                CommentOrder.empty(), false, Instant.now(), Instant.now()
        );
        when(commentRepository.findById(new CommentIdentity(commentId)))
                .thenReturn(Optional.of(sampleCommentRead));
        when(commentRepository.save(any(Comment.class)))
                .thenReturn(updatedComment);

        // when
        Comment result = commentWriter.updateComment(new UserIdentity(userId), commentId, newContent);

        // then
        assertNotNull(result);
        assertEquals(newContent, result.getContent());
        verify(commentRepository).findById(new CommentIdentity(commentId));
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void updateComment_nonExistingComment_throwsCommentNotFoundException() {
        // given
        Long commentId = 999L;
        Long userId = 1L;
        String newContent = "updated content";
        when(commentRepository.findById(new CommentIdentity(commentId)))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(CommentNotFoundException.class, () ->
                commentWriter.updateComment(new UserIdentity(userId), commentId, newContent)
        );
        verify(commentRepository).findById(new CommentIdentity(commentId));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void updateComment_unauthorizedUser_throwsUnauthorizedException() {
        // given
        Long commentId = 1L;
        Long unauthorizedUserId = 999L;
        String newContent = "updated content";
        when(commentRepository.findById(new CommentIdentity(commentId)))
                .thenReturn(Optional.of(sampleCommentRead));

        // when & then
        assertThrows(UnauthorizedException.class, () ->
                commentWriter.updateComment(new UserIdentity(unauthorizedUserId), commentId, newContent)
        );
        verify(commentRepository).findById(new CommentIdentity(commentId));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    // ========== hideComment 테스트 ==========

    @Test
    void hideComment_existingComment_hidesAndSaves() {
        // given
        Long commentId = 1L;
        Long userId = 1L;
        Comment hiddenComment = new Comment(
                1L, 1L, "test content", TargetType.JOB, 1L, null,
                CommentOrder.empty(), true, Instant.now(), Instant.now()
        );
        when(commentRepository.findById(new CommentIdentity(commentId)))
                .thenReturn(Optional.of(sampleCommentRead));
        when(commentRepository.save(any(Comment.class)))
                .thenReturn(hiddenComment);

        // when
        Comment result = commentWriter.hideComment(new UserIdentity(userId), commentId);

        // then
        assertNotNull(result);
        assertTrue(result.getIsHidden());
        verify(commentRepository).findById(new CommentIdentity(commentId));
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void hideComment_nonExistingComment_throwsCommentNotFoundException() {
        // given
        Long commentId = 999L;
        Long userId = 1L;
        when(commentRepository.findById(new CommentIdentity(commentId)))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(CommentNotFoundException.class, () ->
                commentWriter.hideComment(new UserIdentity(userId), commentId)
        );
        verify(commentRepository).findById(new CommentIdentity(commentId));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void hideComment_unauthorizedUser_throwsUnauthorizedException() {
        // given
        Long commentId = 1L;
        Long unauthorizedUserId = 999L;
        when(commentRepository.findById(new CommentIdentity(commentId)))
                .thenReturn(Optional.of(sampleCommentRead));

        // when & then
        assertThrows(UnauthorizedException.class, () ->
                commentWriter.hideComment(new UserIdentity(unauthorizedUserId), commentId)
        );
        verify(commentRepository).findById(new CommentIdentity(commentId));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void hideComment_alreadyHidden_throwsWrongCommentException() {
        // given
        Long commentId = 1L;
        Long userId = 1L;
        CommentRead alreadyHiddenComment = new CommentRead(
                1L, 1L, "testuser", "test content", TargetType.JOB, 1L, null,
                CommentOrder.empty(), true, Instant.now(), Instant.now()
        );
        when(commentRepository.findById(new CommentIdentity(commentId)))
                .thenReturn(Optional.of(alreadyHiddenComment));

        // when & then
        assertThrows(WrongCommentException.class, () ->
                commentWriter.hideComment(new UserIdentity(userId), commentId)
        );
        verify(commentRepository).findById(new CommentIdentity(commentId));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    // ========== showComment 테스트 ==========

    @Test
    void showComment_existingHiddenComment_showsAndSaves() {
        // given
        Long commentId = 1L;
        Long userId = 1L;
        // isHidden=true인 댓글로 테스트 (show할 수 있는 상태)
        CommentRead hiddenCommentRead = new CommentRead(
                1L, 1L, "testuser", "test content", TargetType.JOB, 1L, null,
                CommentOrder.empty(), true, Instant.now(), Instant.now()
        );
        when(commentRepository.findById(new CommentIdentity(commentId)))
                .thenReturn(Optional.of(hiddenCommentRead));
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Comment result = commentWriter.showComment(new UserIdentity(userId), commentId);

        // then
        assertNotNull(result);
        assertFalse(result.getIsHidden());
        verify(commentRepository).findById(new CommentIdentity(commentId));
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void showComment_nonExistingComment_throwsCommentNotFoundException() {
        // given
        Long commentId = 999L;
        Long userId = 1L;
        when(commentRepository.findById(new CommentIdentity(commentId)))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(CommentNotFoundException.class, () ->
                commentWriter.showComment(new UserIdentity(userId), commentId)
        );
        verify(commentRepository).findById(new CommentIdentity(commentId));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void showComment_alreadyShown_throwsWrongCommentException() {
        // given
        Long commentId = 1L;
        Long userId = 1L;
        // isHidden=false인 댓글 (이미 표시된 상태)
        when(commentRepository.findById(new CommentIdentity(commentId)))
                .thenReturn(Optional.of(sampleCommentRead));

        // when & then
        assertThrows(WrongCommentException.class, () ->
                commentWriter.showComment(new UserIdentity(userId), commentId)
        );
        verify(commentRepository).findById(new CommentIdentity(commentId));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void showComment_unauthorizedUser_throwsUnauthorizedException() {
        // given
        Long commentId = 1L;
        Long unauthorizedUserId = 999L;
        CommentRead hiddenCommentRead = new CommentRead(
                1L, 1L, "testuser", "test content", TargetType.JOB, 1L, null,
                CommentOrder.empty(), true, Instant.now(), Instant.now()
        );
        when(commentRepository.findById(new CommentIdentity(commentId)))
                .thenReturn(Optional.of(hiddenCommentRead));

        // when & then
        assertThrows(UnauthorizedException.class, () ->
                commentWriter.showComment(new UserIdentity(unauthorizedUserId), commentId)
        );
        verify(commentRepository).findById(new CommentIdentity(commentId));
        verify(commentRepository, never()).save(any(Comment.class));
    }
}
