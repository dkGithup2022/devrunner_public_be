package dev.devrunner.service.communitypost.impl;

import dev.devrunner.exception.auth.UnauthorizedException;
import dev.devrunner.exception.communitypost.CommunityPostNotFoundException;
import dev.devrunner.infra.communitypost.repository.CommunityPostRepository;
import dev.devrunner.infra.user.repository.UserRepository;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.communitypost.CommunityPost;
import dev.devrunner.model.communitypost.CommunityPostCategory;
import dev.devrunner.model.communitypost.CommunityPostIdentity;
import dev.devrunner.model.communitypost.CommunityPostRead;
import dev.devrunner.model.communitypost.LinkedContent;
import dev.devrunner.model.user.User;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.outbox.command.RecordOutboxEventCommand;
import dev.devrunner.outbox.model.UpdateType;
import dev.devrunner.outbox.recorder.OutboxEventRecorder;
import dev.devrunner.service.communitypost.dto.CommunityPostUpsertCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCommunityPostWriterTest {

    @Mock
    private CommunityPostRepository communityPostRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OutboxEventRecorder outboxEventRecorder;

    @InjectMocks
    private DefaultCommunityPostWriter communityPostWriter;

    private final CommunityPost samplePost = new CommunityPost(
        1L,                                  // communityPostId
        1L,                                  // userId
        CommunityPostCategory.INTERVIEW_SHARE, // category
        "test title",                        // title
        "test markdown body",                // markdownBody
        "test company",                      // company
        "test location",                     // location
        LinkedContent.none(),                // linkedContent
        Popularity.empty(),                  // popularity
        false,                               // isDeleted
        Instant.now(),                       // createdAt
        Instant.now()                        // updatedAt
    );

    @Test
    void upsert_newPost_callsRepositorySaveAndIncrementsUserPostCount() {
        // given
        CommunityPostUpsertCommand command = new CommunityPostUpsertCommand(
            1L,                                  // requestUserId
            null,                                // communityPostId (null = new post)
            CommunityPostCategory.INTERVIEW_SHARE,
            "new title",
            "new markdown body",
            null, null, null, null
        );

        User mockUser = User.newUser("google123", "test@example.com", "testuser");
        User updatedUser = mockUser.incrementPostCount();

        when(communityPostRepository.save(any(CommunityPost.class)))
            .thenReturn(samplePost);
        when(userRepository.findById(any(UserIdentity.class)))
            .thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class)))
            .thenReturn(updatedUser);
        when(outboxEventRecorder.record(any())).thenReturn(null);

        // when
        CommunityPost result = communityPostWriter.upsert(command);

        // then
        assertNotNull(result);
        assertEquals(samplePost.getCommunityPostId(), result.getCommunityPostId());
        verify(communityPostRepository).save(any(CommunityPost.class));
        verify(userRepository).findById(new UserIdentity(1L));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void upsert_existingPost_callsRepositorySaveWithoutIncrementingPostCount() {
        // given
        CommunityPostUpsertCommand command = new CommunityPostUpsertCommand(
            1L,                                  // requestUserId
            1L,                                  // communityPostId (exists = update)
            CommunityPostCategory.INTERVIEW_SHARE,
            "updated title",
            "updated body",
            "test company", "test location", null, null
        );

        CommunityPost updatedPost = new CommunityPost(
            1L, 1L, CommunityPostCategory.INTERVIEW_SHARE,
            "updated title", "updated body",
            "test company", "test location", LinkedContent.none(),
            Popularity.empty(), false, Instant.now(), Instant.now()
        );

        when(communityPostRepository.save(any(CommunityPost.class)))
            .thenReturn(updatedPost);
        when(outboxEventRecorder.record(any())).thenReturn(null);

        // when
        CommunityPost result = communityPostWriter.upsert(command);

        // then
        assertNotNull(result);
        assertEquals("updated title", result.getTitle());
        assertEquals("updated body", result.getMarkdownBody());
        verify(communityPostRepository).save(any(CommunityPost.class));
        verify(userRepository, never()).findById(any(UserIdentity.class));  // User 조회 안 함
        verify(userRepository, never()).save(any(User.class));              // User 저장 안 함
    }

    @Test
    void upsert_postFromJobComment_callsRepositorySaveAndIncrementsUserPostCount() {
        // given
        CommunityPostUpsertCommand command = new CommunityPostUpsertCommand(
            1L,                                  // requestUserId
            null,                                // communityPostId (null = new post)
            CommunityPostCategory.INTERVIEW_SHARE,
            "interview title",
            "interview body",
            "job company", "job location", 100L, null
        );

        CommunityPost fromJobComment = new CommunityPost(
            1L, 1L, CommunityPostCategory.INTERVIEW_SHARE,
            "interview title", "interview body",
            "job company", "job location", LinkedContent.fromJob(100L),
            Popularity.empty(), false, Instant.now(), Instant.now()
        );

        User mockUser = User.newUser("google123", "test@example.com", "testuser");
        User updatedUser = mockUser.incrementPostCount();

        when(communityPostRepository.save(any(CommunityPost.class)))
            .thenReturn(fromJobComment);
        when(userRepository.findById(any(UserIdentity.class)))
            .thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class)))
            .thenReturn(updatedUser);
        when(outboxEventRecorder.record(any())).thenReturn(null);

        // when
        CommunityPost result = communityPostWriter.upsert(command);

        // then
        assertNotNull(result);
        assertTrue(result.getLinkedContent().getIsFromJobComment());
        assertEquals(CommunityPostCategory.INTERVIEW_SHARE, result.getCategory());
        verify(communityPostRepository).save(any(CommunityPost.class));
        verify(userRepository).findById(new UserIdentity(1L));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void delete_existingPost_callsRepositoryDelete() {
        // given
        Long userId = 1L;
        CommunityPostIdentity identity = new CommunityPostIdentity(1L);
        CommunityPostRead samplePostRead = new CommunityPostRead(
            1L, 1L, "testuser", CommunityPostCategory.INTERVIEW_SHARE,
            "test title", "test markdown body", "test company", "test location",
            LinkedContent.none(), Popularity.empty(), false, Instant.now(), Instant.now()
        );
        when(communityPostRepository.findById(identity))
            .thenReturn(Optional.of(samplePostRead));
        doNothing().when(communityPostRepository).deleteById(identity);
        when(outboxEventRecorder.record(any())).thenReturn(null);

        // when
        communityPostWriter.delete(new UserIdentity(userId), identity);

        // then
        verify(communityPostRepository).findById(identity);
        verify(communityPostRepository).deleteById(identity);
    }

    @Test
    void delete_callsRepositoryDeleteWithCorrectIdentity() {
        // given
        Long userId = 1L;
        CommunityPostIdentity identity = new CommunityPostIdentity(999L);
        CommunityPostRead samplePostRead = new CommunityPostRead(
            999L, 1L, "testuser", CommunityPostCategory.INTERVIEW_SHARE,
            "test title", "test markdown body", "test company", "test location",
            LinkedContent.none(), Popularity.empty(), false, Instant.now(), Instant.now()
        );
        when(communityPostRepository.findById(identity))
            .thenReturn(Optional.of(samplePostRead));
        doNothing().when(communityPostRepository).deleteById(identity);
        when(outboxEventRecorder.record(any())).thenReturn(null);

        // when
        communityPostWriter.delete(new UserIdentity(userId), identity);

        // then
        verify(communityPostRepository).findById(identity);
        verify(communityPostRepository).deleteById(identity);
    }

    @Test
    void upsert_newPost_recordsCreatedEvent() {
        // given
        CommunityPostUpsertCommand command = new CommunityPostUpsertCommand(
            1L, null, CommunityPostCategory.INTERVIEW_SHARE,
            "new title", "new markdown body", null, null, null, null
        );

        User mockUser = User.newUser("google123", "test@example.com", "testuser");
        User updatedUser = mockUser.incrementPostCount();

        when(communityPostRepository.save(any(CommunityPost.class)))
            .thenReturn(samplePost);
        when(userRepository.findById(any(UserIdentity.class)))
            .thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class)))
            .thenReturn(updatedUser);

        ArgumentCaptor<RecordOutboxEventCommand> eventCaptor = ArgumentCaptor.forClass(RecordOutboxEventCommand.class);

        // when
        communityPostWriter.upsert(command);

        // then
        verify(outboxEventRecorder).record(eventCaptor.capture());
        RecordOutboxEventCommand capturedEvent = eventCaptor.getValue();
        assertEquals(TargetType.COMMUNITY_POST, capturedEvent.getTargetType());
        assertEquals(1L, capturedEvent.getTargetId());
        assertEquals(UpdateType.CREATED, capturedEvent.getUpdateType());
    }

    @Test
    void upsert_existingPost_recordsUpdatedEvent() {
        // given
        CommunityPostUpsertCommand command = new CommunityPostUpsertCommand(
            1L, 1L, CommunityPostCategory.INTERVIEW_SHARE,
            "updated title", "updated body", "test company", "test location", null, null
        );

        CommunityPost updatedPost = new CommunityPost(
            1L, 1L, CommunityPostCategory.INTERVIEW_SHARE,
            "updated title", "updated body", "test company", "test location",
            LinkedContent.none(), Popularity.empty(), false, Instant.now(), Instant.now()
        );

        when(communityPostRepository.save(any(CommunityPost.class)))
            .thenReturn(updatedPost);

        ArgumentCaptor<RecordOutboxEventCommand> eventCaptor = ArgumentCaptor.forClass(RecordOutboxEventCommand.class);

        // when
        communityPostWriter.upsert(command);

        // then
        verify(outboxEventRecorder).record(eventCaptor.capture());
        RecordOutboxEventCommand capturedEvent = eventCaptor.getValue();
        assertEquals(TargetType.COMMUNITY_POST, capturedEvent.getTargetType());
        assertEquals(1L, capturedEvent.getTargetId());
        assertEquals(UpdateType.UPDATED, capturedEvent.getUpdateType());
    }

    @Test
    void delete_recordsUpdatedEvent() {
        // given
        Long userId = 1L;
        CommunityPostIdentity identity = new CommunityPostIdentity(1L);
        CommunityPostRead samplePostRead = new CommunityPostRead(
            1L, 1L, "testuser", CommunityPostCategory.INTERVIEW_SHARE,
            "test title", "test markdown body", "test company", "test location",
            LinkedContent.none(), Popularity.empty(), false, Instant.now(), Instant.now()
        );

        when(communityPostRepository.findById(identity))
            .thenReturn(Optional.of(samplePostRead));
        doNothing().when(communityPostRepository).deleteById(identity);

        ArgumentCaptor<RecordOutboxEventCommand> eventCaptor = ArgumentCaptor.forClass(RecordOutboxEventCommand.class);

        // when
        communityPostWriter.delete(new UserIdentity(userId), identity);

        // then
        verify(outboxEventRecorder).record(eventCaptor.capture());
        RecordOutboxEventCommand capturedEvent = eventCaptor.getValue();
        assertEquals(TargetType.COMMUNITY_POST, capturedEvent.getTargetType());
        assertEquals(1L, capturedEvent.getTargetId());
        assertEquals(UpdateType.UPDATED, capturedEvent.getUpdateType());
    }

    @Test
    void upsert_userNotFound_throwsRuntimeException() {
        // given
        CommunityPostUpsertCommand command = new CommunityPostUpsertCommand(
            1L, null, CommunityPostCategory.INTERVIEW_SHARE,
            "new title", "new markdown body", null, null, null, null
        );

        when(communityPostRepository.save(any(CommunityPost.class)))
            .thenReturn(samplePost);
        when(userRepository.findById(any(UserIdentity.class)))
            .thenReturn(Optional.empty()); // User not found

        // when & then
        assertThrows(RuntimeException.class, () ->
            communityPostWriter.upsert(command)
        );

        verify(communityPostRepository).save(any(CommunityPost.class));
        verify(userRepository).findById(new UserIdentity(1L));
        verify(userRepository, never()).save(any(User.class));
        verify(outboxEventRecorder, never()).record(any());
    }

    @Test
    void delete_postNotFound_throwsCommunityPostNotFoundException() {
        // given
        Long userId = 1L;
        CommunityPostIdentity identity = new CommunityPostIdentity(1L);
        when(communityPostRepository.findById(identity))
            .thenReturn(Optional.empty());

        // when & then
        assertThrows(CommunityPostNotFoundException.class, () ->
            communityPostWriter.delete(new UserIdentity(userId), identity)
        );

        verify(communityPostRepository).findById(identity);
        verify(communityPostRepository, never()).deleteById(any());
        verify(outboxEventRecorder, never()).record(any());
    }

    @Test
    void delete_unauthorizedUser_throwsUnauthorizedException() {
        // given
        Long requestUserId = 2L; // different user
        Long postOwnerUserId = 1L;
        CommunityPostIdentity identity = new CommunityPostIdentity(1L);
        CommunityPostRead samplePostRead = new CommunityPostRead(
            1L, postOwnerUserId, "testuser", CommunityPostCategory.INTERVIEW_SHARE,
            "test title", "test markdown body", "test company", "test location",
            LinkedContent.none(), Popularity.empty(), false, Instant.now(), Instant.now()
        );

        when(communityPostRepository.findById(identity))
            .thenReturn(Optional.of(samplePostRead));

        // when & then
        assertThrows(UnauthorizedException.class, () ->
            communityPostWriter.delete(new UserIdentity(requestUserId), identity)
        );

        verify(communityPostRepository).findById(identity);
        verify(communityPostRepository, never()).deleteById(any());
        verify(outboxEventRecorder, never()).record(any());
    }

    @Test
    void upsert_linkedContentFromJob_createsLinkedContent() {
        // given
        CommunityPostUpsertCommand command = new CommunityPostUpsertCommand(
            1L, null, CommunityPostCategory.INTERVIEW_SHARE,
            "job linked title", "job linked body",
            "test company", "test location", 100L, null // jobId = 100L, commentId = null
        );

        CommunityPost postWithJobLink = new CommunityPost(
            1L, 1L, CommunityPostCategory.INTERVIEW_SHARE,
            "job linked title", "job linked body",
            "test company", "test location", LinkedContent.fromJob(100L),
            Popularity.empty(), false, Instant.now(), Instant.now()
        );

        User mockUser = User.newUser("google123", "test@example.com", "testuser");
        User updatedUser = mockUser.incrementPostCount();

        when(communityPostRepository.save(any(CommunityPost.class)))
            .thenReturn(postWithJobLink);
        when(userRepository.findById(any(UserIdentity.class)))
            .thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class)))
            .thenReturn(updatedUser);
        when(outboxEventRecorder.record(any())).thenReturn(null);

        // when
        CommunityPost result = communityPostWriter.upsert(command);

        // then
        assertNotNull(result);
        assertTrue(result.getLinkedContent().getIsFromJobComment());
        assertEquals(100L, result.getLinkedContent().getJobId().getJobId());
        verify(communityPostRepository).save(any(CommunityPost.class));
    }

    @Test
    void upsert_linkedContentFromJobComment_createsLinkedContent() {
        // given
        CommunityPostUpsertCommand command = new CommunityPostUpsertCommand(
            1L, null, CommunityPostCategory.INTERVIEW_SHARE,
            "job comment linked title", "job comment linked body",
            "test company", "test location", 100L, 200L // jobId = 100L, commentId = 200L
        );

        CommunityPost postWithJobCommentLink = new CommunityPost(
            1L, 1L, CommunityPostCategory.INTERVIEW_SHARE,
            "job comment linked title", "job comment linked body",
            "test company", "test location", LinkedContent.fromJobComment(100L, 200L),
            Popularity.empty(), false, Instant.now(), Instant.now()
        );

        User mockUser = User.newUser("google123", "test@example.com", "testuser");
        User updatedUser = mockUser.incrementPostCount();

        when(communityPostRepository.save(any(CommunityPost.class)))
            .thenReturn(postWithJobCommentLink);
        when(userRepository.findById(any(UserIdentity.class)))
            .thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class)))
            .thenReturn(updatedUser);
        when(outboxEventRecorder.record(any())).thenReturn(null);

        // when
        CommunityPost result = communityPostWriter.upsert(command);

        // then
        assertNotNull(result);
        assertTrue(result.getLinkedContent().getIsFromJobComment());
        assertEquals(100L, result.getLinkedContent().getJobId().getJobId());
        assertEquals(200L, result.getLinkedContent().getCommentId().getCommentId());
        verify(communityPostRepository).save(any(CommunityPost.class));
    }

    @Test
    void upsert_noLinkedContent_createsNoneLinkedContent() {
        // given
        CommunityPostUpsertCommand command = new CommunityPostUpsertCommand(
            1L, null, CommunityPostCategory.INTERVIEW_SHARE,
            "no linked title", "no linked body",
            "test company", "test location", null, null // no jobId, no commentId
        );

        CommunityPost postWithNoLink = new CommunityPost(
            1L, 1L, CommunityPostCategory.INTERVIEW_SHARE,
            "no linked title", "no linked body",
            "test company", "test location", LinkedContent.none(),
            Popularity.empty(), false, Instant.now(), Instant.now()
        );

        User mockUser = User.newUser("google123", "test@example.com", "testuser");
        User updatedUser = mockUser.incrementPostCount();

        when(communityPostRepository.save(any(CommunityPost.class)))
            .thenReturn(postWithNoLink);
        when(userRepository.findById(any(UserIdentity.class)))
            .thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class)))
            .thenReturn(updatedUser);
        when(outboxEventRecorder.record(any())).thenReturn(null);

        // when
        CommunityPost result = communityPostWriter.upsert(command);

        // then
        assertNotNull(result);
        assertFalse(result.getLinkedContent().getIsFromJobComment());
        assertNull(result.getLinkedContent().getJobId());
        verify(communityPostRepository).save(any(CommunityPost.class));
    }
}
