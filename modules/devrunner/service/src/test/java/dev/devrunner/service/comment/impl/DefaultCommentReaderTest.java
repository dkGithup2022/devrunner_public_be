package dev.devrunner.service.comment.impl;

import dev.devrunner.exception.comment.CommentNotFoundException;
import dev.devrunner.infra.comment.repository.CommentRepository;
import dev.devrunner.model.comment.CommentIdentity;
import dev.devrunner.model.comment.CommentRead;
import dev.devrunner.model.common.CommentOrder;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.user.UserIdentity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCommentReaderTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private DefaultCommentReader commentReader;

    private final CommentRead sampleCommentRead = new CommentRead(
        1L,                          // commentId
        1L,                          // userId
        "testuser",                  // nickname
        "test content",              // content
        TargetType.JOB,              // targetType
        100L,                        // targetId
        null,                        // parentId
        CommentOrder.newRootComment(1), // commentOrder
        false,                       // isHidden
        Instant.now(),               // createdAt
        Instant.now()                // updatedAt
    );

    private final CommentIdentity testIdentity = new CommentIdentity(1L);
    private final UserIdentity testUserIdentity = new UserIdentity(1L);

    // ========== getById 테스트 ==========

    @Test
    void getById_existingId_returnsCommentRead() {
        // given
        when(commentRepository.findById(testIdentity))
            .thenReturn(Optional.of(sampleCommentRead));

        // when
        CommentRead result = commentReader.getById(testIdentity);

        // then
        assertNotNull(result);
        assertEquals(sampleCommentRead.getCommentId(), result.getCommentId());
        assertEquals(sampleCommentRead.getContent(), result.getContent());
        verify(commentRepository).findById(testIdentity);
    }

    @Test
    void getById_nonExistingId_throwsCommentNotFoundException() {
        // given
        when(commentRepository.findById(testIdentity))
            .thenReturn(Optional.empty());

        // when & then
        assertThrows(CommentNotFoundException.class, () ->
            commentReader.getById(testIdentity)
        );
        verify(commentRepository).findById(testIdentity);
    }

    // ========== getByUserId 테스트 ==========

    @Test
    void getByUserId_existingUser_returnsList() {
        // given
        int page = 0;
        int size = 20;
        List<CommentRead> comments = List.of(sampleCommentRead);
        when(commentRepository.findByUserId(testUserIdentity, page, size))
            .thenReturn(comments);

        // when
        List<CommentRead> result = commentReader.getByUserId(testUserIdentity, page, size);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserIdentity.getUserId(), result.get(0).getUserId());
        verify(commentRepository).findByUserId(testUserIdentity, page, size);
    }

    @Test
    void getByUserId_nonExistingUser_returnsEmptyList() {
        // given
        UserIdentity nonExistingUser = new UserIdentity(999L);
        int page = 0;
        int size = 20;
        when(commentRepository.findByUserId(nonExistingUser, page, size))
            .thenReturn(List.of());

        // when
        List<CommentRead> result = commentReader.getByUserId(nonExistingUser, page, size);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(commentRepository).findByUserId(nonExistingUser, page, size);
    }

    // ========== getAll 테스트 ==========

    @Test
    void getAll_existingComments_returnsList() {
        // given
        List<CommentRead> comments = List.of(sampleCommentRead);
        when(commentRepository.findAll())
            .thenReturn(comments);

        // when
        List<CommentRead> result = commentReader.getAll();

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(commentRepository).findAll();
    }

    @Test
    void getAll_noComments_returnsEmptyList() {
        // given
        when(commentRepository.findAll())
            .thenReturn(List.of());

        // when
        List<CommentRead> result = commentReader.getAll();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(commentRepository).findAll();
    }

    // ========== getByTargetTypeAndTargetId 테스트 ==========

    @Test
    void getByTargetTypeAndTargetId_existingData_returnsList() {
        // given
        TargetType targetType = TargetType.JOB;
        Long targetId = 100L;
        List<CommentRead> comments = List.of(sampleCommentRead);
        when(commentRepository.findByTargetTypeAndTargetId(targetType, targetId))
            .thenReturn(comments);

        // when
        List<CommentRead> result = commentReader.getByTargetTypeAndTargetId(targetType, targetId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(targetType, result.get(0).getTargetType());
        assertEquals(targetId, result.get(0).getTargetId());
        verify(commentRepository).findByTargetTypeAndTargetId(targetType, targetId);
    }

    @Test
    void getByTargetTypeAndTargetId_nonExistingData_returnsEmptyList() {
        // given
        TargetType targetType = TargetType.TECH_BLOG;
        Long targetId = 999L;
        when(commentRepository.findByTargetTypeAndTargetId(targetType, targetId))
            .thenReturn(List.of());

        // when
        List<CommentRead> result = commentReader.getByTargetTypeAndTargetId(targetType, targetId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(commentRepository).findByTargetTypeAndTargetId(targetType, targetId);
    }

    // ========== getByParentId 테스트 ==========

    @Test
    void getByParentId_existingParent_returnsList() {
        // given
        Long parentId = 1L;
        CommentRead replyComment = new CommentRead(
            2L, 2L, "testuser2", "reply content",
            TargetType.JOB, 100L, parentId, CommentOrder.newRootComment(1),
            false, Instant.now(), Instant.now()
        );
        List<CommentRead> replies = List.of(replyComment);
        when(commentRepository.findByParentId(parentId))
            .thenReturn(replies);

        // when
        List<CommentRead> result = commentReader.getByParentId(parentId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(parentId, result.get(0).getParentId());
        verify(commentRepository).findByParentId(parentId);
    }

    @Test
    void getByParentId_nonExistingParent_returnsEmptyList() {
        // given
        Long parentId = 999L;
        when(commentRepository.findByParentId(parentId))
            .thenReturn(List.of());

        // when
        List<CommentRead> result = commentReader.getByParentId(parentId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(commentRepository).findByParentId(parentId);
    }
}
