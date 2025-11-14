package dev.devrunner.api.comment;

import dev.devrunner.api.comment.dto.CommentResponse;
import dev.devrunner.api.comment.dto.CommentUpdateRequest;
import dev.devrunner.api.comment.dto.CommentWriteRequest;
import dev.devrunner.auth.model.SessionUser;
import dev.devrunner.model.comment.Comment;
import dev.devrunner.model.comment.CommentIdentity;
import dev.devrunner.model.comment.CommentRead;
import dev.devrunner.model.common.CommentOrder;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.service.comment.CommentReader;
import dev.devrunner.service.comment.CommentWriter;
import dev.devrunner.service.comment.dto.CommentWriteCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * CommentApiController 테스트
 *
 * @ExtendWith(MockitoExtension.class) 사용
 * MockMvc 없이 Controller를 직접 호출하여 테스트
 */
@ExtendWith(MockitoExtension.class)
class CommentApiControllerTest {

    @Mock
    private CommentReader commentReader;

    @Mock
    private CommentWriter commentWriter;

    @InjectMocks
    private CommentApiController controller;

    // ========== getCommentsByTarget 테스트 ==========

    @Test
    void getCommentsByTarget_success() {
        // given
        TargetType targetType = TargetType.JOB;
        Long targetId = 100L;

        List<CommentRead> comments = List.of(
                createCommentRead(1L, 1L, "TestUser1", "Comment 1"),
                createCommentRead(2L, 2L, "TestUser2", "Comment 2")
        );

        when(commentReader.getByTargetTypeAndTargetId(eq(targetType), eq(targetId)))
                .thenReturn(comments);

        // when
        ResponseEntity<List<CommentResponse>> response = controller.getCommentsByTarget(targetType, targetId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        verify(commentReader).getByTargetTypeAndTargetId(eq(targetType), eq(targetId));
    }

    // ========== writeComment 테스트 ==========

    @Test
    void writeComment_success() {
        // given
        SessionUser sessionUser = SessionUser.of(1L, Instant.now(), Instant.now().plusSeconds(3600));
        CommentWriteRequest request = createCommentWriteRequest(
                "Test comment content",
                TargetType.JOB,
                100L,
                null
        );

        Comment comment = createComment(1L, 1L, "Test comment content");
        CommentRead commentRead = createCommentRead(1L, 1L, "TestUser", "Test comment content");

        when(commentWriter.write(any(CommentWriteCommand.class))).thenReturn(comment);
        when(commentReader.getById(any(CommentIdentity.class))).thenReturn(commentRead);

        // when
        ResponseEntity<CommentResponse> response = controller.writeComment(request, sessionUser);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCommentId()).isEqualTo(1L);
        assertThat(response.getBody().getContent()).isEqualTo("Test comment content");
        verify(commentWriter).write(any(CommentWriteCommand.class));
        verify(commentReader).getById(any(CommentIdentity.class));
    }

    // ========== updateComment 테스트 ==========

    @Test
    void updateComment_success() {
        // given
        SessionUser sessionUser = SessionUser.of(1L, Instant.now(), Instant.now().plusSeconds(3600));
        Long commentId = 1L;
        CommentUpdateRequest request = createCommentUpdateRequest("Updated content");

        Comment comment = createComment(commentId, 1L, "Updated content");
        CommentRead commentRead = createCommentRead(commentId, 1L, "TestUser", "Updated content");

        when(commentWriter.updateComment(any(UserIdentity.class), eq(commentId), eq("Updated content")))
                .thenReturn(comment);
        when(commentReader.getById(any(CommentIdentity.class))).thenReturn(commentRead);

        // when
        ResponseEntity<CommentResponse> response = controller.updateComment(sessionUser, commentId, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCommentId()).isEqualTo(commentId);
        assertThat(response.getBody().getContent()).isEqualTo("Updated content");
        verify(commentWriter).updateComment(any(UserIdentity.class), eq(commentId), eq("Updated content"));
        verify(commentReader).getById(any(CommentIdentity.class));
    }

    // ========== hideComment 테스트 ==========

    @Test
    void hideComment_success() {
        // given
        SessionUser sessionUser = SessionUser.of(1L, Instant.now(), Instant.now().plusSeconds(3600));
        Long commentId = 1L;

        Comment comment = createComment(commentId, 1L, "Hidden comment");
        CommentRead commentRead = createCommentRead(commentId, 1L, "TestUser", "this comment has been hidden");

        when(commentWriter.hideComment(any(UserIdentity.class), eq(commentId)))
                .thenReturn(comment);
        when(commentReader.getById(any(CommentIdentity.class))).thenReturn(commentRead);

        // when
        ResponseEntity<CommentResponse> response = controller.hideComment(sessionUser, commentId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCommentId()).isEqualTo(commentId);
        verify(commentWriter).hideComment(any(UserIdentity.class), eq(commentId));
        verify(commentReader).getById(any(CommentIdentity.class));
    }

    // ========== showComment 테스트 ==========

    @Test
    void showComment_success() {
        // given
        SessionUser sessionUser = SessionUser.of(1L, Instant.now(), Instant.now().plusSeconds(3600));
        Long commentId = 1L;

        Comment comment = createComment(commentId, 1L, "Visible comment");
        CommentRead commentRead = createCommentRead(commentId, 1L, "TestUser", "Visible comment");

        when(commentWriter.showComment(any(UserIdentity.class), eq(commentId)))
                .thenReturn(comment);
        when(commentReader.getById(any(CommentIdentity.class))).thenReturn(commentRead);

        // when
        ResponseEntity<CommentResponse> response = controller.showComment(sessionUser, commentId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCommentId()).isEqualTo(commentId);
        assertThat(response.getBody().getContent()).isEqualTo("Visible comment");
        verify(commentWriter).showComment(any(UserIdentity.class), eq(commentId));
        verify(commentReader).getById(any(CommentIdentity.class));
    }

    // ========== 헬퍼 메서드 ==========

    private CommentWriteRequest createCommentWriteRequest(String content, TargetType targetType, Long targetId, Long parentId) {
        CommentWriteRequest request = new CommentWriteRequest();
        try {
            var contentField = CommentWriteRequest.class.getDeclaredField("content");
            contentField.setAccessible(true);
            contentField.set(request, content);

            var targetTypeField = CommentWriteRequest.class.getDeclaredField("targetType");
            targetTypeField.setAccessible(true);
            targetTypeField.set(request, targetType);

            var targetIdField = CommentWriteRequest.class.getDeclaredField("targetId");
            targetIdField.setAccessible(true);
            targetIdField.set(request, targetId);

            var parentIdField = CommentWriteRequest.class.getDeclaredField("parentId");
            parentIdField.setAccessible(true);
            parentIdField.set(request, parentId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return request;
    }

    private CommentUpdateRequest createCommentUpdateRequest(String content) {
        CommentUpdateRequest request = new CommentUpdateRequest();
        try {
            var contentField = CommentUpdateRequest.class.getDeclaredField("content");
            contentField.setAccessible(true);
            contentField.set(request, content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return request;
    }

    private Comment createComment(Long commentId, Long userId, String content) {
        return new Comment(
                commentId,
                userId,
                content,
                TargetType.JOB,
                100L,
                null,
                CommentOrder.newRootComment(1),
                false,
                Instant.now(),
                Instant.now()
        );
    }

    private CommentRead createCommentRead(Long commentId, Long userId, String nickname, String content) {
        return new CommentRead(
                commentId,
                userId,
                nickname,
                content,
                TargetType.JOB,
                100L,
                null,
                CommentOrder.newRootComment(1),
                false,
                Instant.now(),
                Instant.now()
        );
    }
}
