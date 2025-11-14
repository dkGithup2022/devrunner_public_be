package dev.devrunner.api.reaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.api.reaction.dto.ReactionRequest;
import dev.devrunner.auth.model.SessionUser;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.service.reaction.ReactionWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactionApiControllerTest {

    @Mock
    private ReactionWriter reactionWriter;

    @InjectMocks
    private ReactionApiController reactionApiController;

    private ObjectMapper objectMapper;

    private final SessionUser sampleSessionUser = SessionUser.of(1L, Instant.now(), Instant.now().plusSeconds(1000 * 60));

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // JavaTimeModule 등록
    }

    @Test
    void likeUp_validRequest_returnsOk() {
        // given
        ReactionRequest request = new ReactionRequest(TargetType.JOB, 100L);
        doNothing().when(reactionWriter).likeUp(new UserIdentity(1L), TargetType.JOB, 100L);

        // when
        ResponseEntity<Void> response = reactionApiController.likeUp(sampleSessionUser, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(reactionWriter).likeUp(new UserIdentity(1L), TargetType.JOB, 100L);
    }

    @Test
    void likeUp_exception_throwsException() {
        // given
        ReactionRequest request = new ReactionRequest(TargetType.JOB, 100L);
        doThrow(new RuntimeException("Like failed"))
                .when(reactionWriter).likeUp(new UserIdentity(1L), TargetType.JOB, 100L);

        // when & then
        try {
            reactionApiController.likeUp(sampleSessionUser, request);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Like failed");
        }

        verify(reactionWriter).likeUp(new UserIdentity(1L), TargetType.JOB, 100L);
    }

    @Test
    void dislikeUp_validRequest_returnsOk() {
        // given
        ReactionRequest request = new ReactionRequest(TargetType.JOB, 100L);
        doNothing().when(reactionWriter).dislikeUp(new UserIdentity(1L), TargetType.JOB, 100L);

        // when
        ResponseEntity<Void> response = reactionApiController.dislikeUp(sampleSessionUser, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(reactionWriter).dislikeUp(new UserIdentity(1L), TargetType.JOB, 100L);
    }

    @Test
    void dislikeUp_exception_throwsException() {
        // given
        ReactionRequest request = new ReactionRequest(TargetType.JOB, 100L);
        doThrow(new RuntimeException("Dislike failed"))
                .when(reactionWriter).dislikeUp(new UserIdentity(1L), TargetType.JOB, 100L);

        // when & then
        try {
            reactionApiController.dislikeUp(sampleSessionUser, request);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Dislike failed");
        }

        verify(reactionWriter).dislikeUp(new UserIdentity(1L), TargetType.JOB, 100L);
    }

    @Test
    void likeDown_validRequest_returnsNoContent() {
        // given
        ReactionRequest request = new ReactionRequest(TargetType.JOB, 100L);
        doNothing().when(reactionWriter).likeDown(new UserIdentity(1L), TargetType.JOB, 100L);

        // when
        ResponseEntity<Void> response = reactionApiController.likeDown(sampleSessionUser, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(reactionWriter).likeDown(new UserIdentity(1L), TargetType.JOB, 100L);
    }

    @Test
    void likeDown_exception_throwsException() {
        // given
        ReactionRequest request = new ReactionRequest(TargetType.JOB, 100L);
        doThrow(new RuntimeException("Like removal failed"))
                .when(reactionWriter).likeDown(new UserIdentity(1L), TargetType.JOB, 100L);

        // when & then
        try {
            reactionApiController.likeDown(sampleSessionUser, request);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Like removal failed");
        }

        verify(reactionWriter).likeDown(new UserIdentity(1L), TargetType.JOB, 100L);
    }

    @Test
    void dislikeDown_validRequest_returnsNoContent() {
        // given
        ReactionRequest request = new ReactionRequest(TargetType.JOB, 100L);
        doNothing().when(reactionWriter).dislikeDown(new UserIdentity(1L), TargetType.JOB, 100L);

        // when
        ResponseEntity<Void> response = reactionApiController.dislikeDown(sampleSessionUser, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(reactionWriter).dislikeDown(new UserIdentity(1L), TargetType.JOB, 100L);
    }

    @Test
    void dislikeDown_exception_throwsException() {
        // given
        ReactionRequest request = new ReactionRequest(TargetType.JOB, 100L);
        doThrow(new RuntimeException("Dislike removal failed"))
                .when(reactionWriter).dislikeDown(new UserIdentity(1L), TargetType.JOB, 100L);

        // when & then
        try {
            reactionApiController.dislikeDown(sampleSessionUser, request);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Dislike removal failed");
        }

        verify(reactionWriter).dislikeDown(new UserIdentity(1L), TargetType.JOB, 100L);
    }
}
