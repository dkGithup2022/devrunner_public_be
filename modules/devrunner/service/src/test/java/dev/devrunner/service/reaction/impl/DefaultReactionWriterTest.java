package dev.devrunner.service.reaction.impl;

import dev.devrunner.exception.reaction.ReactionConflictException;
import dev.devrunner.infra.communitypost.repository.CommunityPostRepository;
import dev.devrunner.infra.job.repository.JobRepository;
import dev.devrunner.infra.reaction.repository.ReactionRepository;
import dev.devrunner.infra.user.repository.UserRepository;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.communitypost.CommunityPostCategory;
import dev.devrunner.model.communitypost.CommunityPostIdentity;
import dev.devrunner.model.communitypost.CommunityPostRead;
import dev.devrunner.model.communitypost.LinkedContent;
import dev.devrunner.model.job.JobIdentity;
import dev.devrunner.model.reaction.Reaction;
import dev.devrunner.model.reaction.ReactionIdentity;
import dev.devrunner.model.reaction.ReactionType;
import dev.devrunner.model.user.User;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.service.reaction.ReactionReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultReactionWriterTest {

    @Mock
    private ReactionReader reactionReader;

    @Mock
    private ReactionRepository reactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CommunityPostRepository communityPostRepository;

    @InjectMocks
    private DefaultReactionWriter reactionWriter;

    private final Long userId = 1L;
    private final Long authorId = 2L;
    private final Long targetId = 100L;

    private final Reaction likeReactionOnJob = new Reaction(
        1L, userId, TargetType.JOB, targetId, ReactionType.LIKE,
        Instant.now(), Instant.now()
    );

    private final Reaction dislikeReactionOnJob = new Reaction(
        1L, userId, TargetType.JOB, targetId, ReactionType.DISLIKE,
        Instant.now(), Instant.now()
    );

    private final Reaction likeReactionOnCommunityPost = new Reaction(
        1L, userId, TargetType.COMMUNITY_POST, targetId, ReactionType.LIKE,
        Instant.now(), Instant.now()
    );

    private final Reaction dislikeReactionOnCommunityPost = new Reaction(
        1L, userId, TargetType.COMMUNITY_POST, targetId, ReactionType.DISLIKE,
        Instant.now(), Instant.now()
    );

    private final CommunityPostRead sampleCommunityPost = new CommunityPostRead(
        targetId, authorId, "author", CommunityPostCategory.INTERVIEW_SHARE,
        "title", "body", "company", "location", LinkedContent.none(),
        Popularity.empty(), false, Instant.now(), Instant.now()
    );

    @Test
    void likeUp_noExistingReaction_onJob_createsNewLikeAndIncrementsUserLikeGivenCount() {
        // given
        User mockUser = User.newUser("google123", "test@example.com", "testuser");
        User updatedUser = mockUser.incrementLikeGivenCount();

        when(reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId))
            .thenReturn(Optional.empty());
        when(reactionRepository.save(any(Reaction.class)))
            .thenReturn(likeReactionOnJob);
        doNothing().when(jobRepository).increaseLikeCount(any(JobIdentity.class), anyLong());
        when(userRepository.findById(any(UserIdentity.class)))
            .thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class)))
            .thenReturn(updatedUser);

        // when
        reactionWriter.likeUp(new UserIdentity(userId), TargetType.JOB, targetId);

        // then
        verify(reactionReader).findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId);
        verify(reactionRepository).save(any(Reaction.class));
        verify(jobRepository).increaseLikeCount(new JobIdentity(targetId), 1L);
        verify(userRepository).findById(new UserIdentity(userId));
        verify(userRepository).save(any(User.class));
        // JOB 타입이므로 CommunityPostRepository는 호출되지 않음
        verify(communityPostRepository, never()).findById(any(CommunityPostIdentity.class));
    }

    @Test
    void likeUp_existingLike_throwsReactionConflictException() {
        // given
        when(reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId))
            .thenReturn(Optional.of(likeReactionOnJob));

        // when & then
        assertThrows(ReactionConflictException.class, () ->
            reactionWriter.likeUp(new UserIdentity(userId), TargetType.JOB, targetId)
        );

        verify(reactionReader).findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId);
        verify(reactionRepository, never()).save(any(Reaction.class));
        verify(userRepository, never()).findById(any(UserIdentity.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void likeUp_existingDislike_onJob_throwsReactionConflictException() {
        // given
        when(reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId))
            .thenReturn(Optional.of(dislikeReactionOnJob));

        // when & then
        assertThrows(ReactionConflictException.class, () ->
            reactionWriter.likeUp(new UserIdentity(userId), TargetType.JOB, targetId)
        );

        verify(reactionReader).findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId);
        verify(reactionRepository, never()).save(any(Reaction.class));
        verify(userRepository, never()).findById(any(UserIdentity.class));
        verify(userRepository, never()).save(any(User.class));
        verify(communityPostRepository, never()).findById(any(CommunityPostIdentity.class));
    }

    @Test
    void likeUp_noExistingReaction_onCommunityPost_incrementsBothUserAndAuthorMetrics() {
        // given
        User mockUser = User.newUser("google123", "test@example.com", "testuser");
        User updatedUser = mockUser.incrementLikeGivenCount();

        User mockAuthor = User.newUser("google456", "author@example.com", "author");
        User updatedAuthor = mockAuthor.incrementLikesReceived();

        when(reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.COMMUNITY_POST, targetId))
            .thenReturn(Optional.empty());
        when(reactionRepository.save(any(Reaction.class)))
            .thenReturn(likeReactionOnCommunityPost);
        doNothing().when(communityPostRepository).increaseLikeCount(any(CommunityPostIdentity.class), anyLong());
        when(userRepository.findById(new UserIdentity(userId)))
            .thenReturn(Optional.of(mockUser));
        when(userRepository.findById(new UserIdentity(authorId)))
            .thenReturn(Optional.of(mockAuthor));
        when(userRepository.save(any(User.class)))
            .thenReturn(updatedUser, updatedAuthor);
        when(communityPostRepository.findById(new CommunityPostIdentity(targetId)))
            .thenReturn(Optional.of(sampleCommunityPost));

        // when
        reactionWriter.likeUp(new UserIdentity(userId), TargetType.COMMUNITY_POST, targetId);

        // then
        verify(reactionRepository).save(any(Reaction.class));
        verify(communityPostRepository).increaseLikeCount(new CommunityPostIdentity(targetId), 1L);
        verify(userRepository).findById(new UserIdentity(userId));
        verify(communityPostRepository).findById(new CommunityPostIdentity(targetId));
        verify(userRepository).findById(new UserIdentity(authorId));
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void likeUp_existingDislike_onCommunityPost_throwsReactionConflictException() {
        // given
        when(reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.COMMUNITY_POST, targetId))
            .thenReturn(Optional.of(dislikeReactionOnCommunityPost));

        // when & then
        assertThrows(ReactionConflictException.class, () ->
            reactionWriter.likeUp(new UserIdentity(userId), TargetType.COMMUNITY_POST, targetId)
        );

        verify(reactionReader).findByUserIdAndTargetTypeAndTargetId(userId, TargetType.COMMUNITY_POST, targetId);
        verify(reactionRepository, never()).save(any(Reaction.class));
        verify(userRepository, never()).findById(any(UserIdentity.class));
        verify(userRepository, never()).save(any(User.class));
        verify(communityPostRepository, never()).findById(any(CommunityPostIdentity.class));
    }

    @Test
    void likeUp_onCommunityPost_authorDeleted_incrementsOnlyUserMetrics() {
        // given
        User mockUser = User.newUser("google123", "test@example.com", "testuser");
        User updatedUser = mockUser.incrementLikeGivenCount();

        when(reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.COMMUNITY_POST, targetId))
            .thenReturn(Optional.empty());
        when(reactionRepository.save(any(Reaction.class)))
            .thenReturn(likeReactionOnCommunityPost);
        doNothing().when(communityPostRepository).increaseLikeCount(any(CommunityPostIdentity.class), anyLong());
        when(userRepository.findById(new UserIdentity(userId)))
            .thenReturn(Optional.of(mockUser));
        when(userRepository.findById(new UserIdentity(authorId)))
            .thenReturn(Optional.empty()); // 작성자 탈퇴
        when(userRepository.save(any(User.class)))
            .thenReturn(updatedUser);
        when(communityPostRepository.findById(new CommunityPostIdentity(targetId)))
            .thenReturn(Optional.of(sampleCommunityPost));

        // when
        reactionWriter.likeUp(new UserIdentity(userId), TargetType.COMMUNITY_POST, targetId);

        // then
        verify(reactionRepository).save(any(Reaction.class));
        verify(communityPostRepository).increaseLikeCount(new CommunityPostIdentity(targetId), 1L);
        verify(userRepository).findById(new UserIdentity(userId));
        verify(communityPostRepository).findById(new CommunityPostIdentity(targetId));
        verify(userRepository).findById(new UserIdentity(authorId));
        verify(userRepository, times(1)).save(any(User.class)); // User만 저장, Author는 스킵
    }

    @Test
    void likeUp_onCommunityPost_postDeleted_incrementsOnlyUserMetrics() {
        // given
        User mockUser = User.newUser("google123", "test@example.com", "testuser");
        User updatedUser = mockUser.incrementLikeGivenCount();

        when(reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.COMMUNITY_POST, targetId))
            .thenReturn(Optional.empty());
        when(reactionRepository.save(any(Reaction.class)))
            .thenReturn(likeReactionOnCommunityPost);
        doNothing().when(communityPostRepository).increaseLikeCount(any(CommunityPostIdentity.class), anyLong());
        when(userRepository.findById(new UserIdentity(userId)))
            .thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class)))
            .thenReturn(updatedUser);
        when(communityPostRepository.findById(new CommunityPostIdentity(targetId)))
            .thenReturn(Optional.empty()); // 게시글 삭제됨

        // when
        reactionWriter.likeUp(new UserIdentity(userId), TargetType.COMMUNITY_POST, targetId);

        // then
        verify(reactionRepository).save(any(Reaction.class));
        verify(communityPostRepository).increaseLikeCount(new CommunityPostIdentity(targetId), 1L);
        verify(userRepository).findById(new UserIdentity(userId));
        verify(communityPostRepository).findById(new CommunityPostIdentity(targetId));
        verify(userRepository, never()).findById(new UserIdentity(authorId)); // 게시글 없으면 Author 조회 안함
        verify(userRepository, times(1)).save(any(User.class)); // User만 저장
    }

    @Test
    void dislikeUp_noExistingReaction_createsNewDislikeWithoutUserMetricUpdate() {
        // given
        when(reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId))
            .thenReturn(Optional.empty());
        when(reactionRepository.save(any(Reaction.class)))
            .thenReturn(dislikeReactionOnJob);
        doNothing().when(jobRepository).increaseDislikeCount(any(JobIdentity.class), anyLong());

        // when
        reactionWriter.dislikeUp(new UserIdentity(userId), TargetType.JOB, targetId);

        // then
        verify(reactionReader).findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId);
        verify(reactionRepository).save(any(Reaction.class));
        verify(jobRepository).increaseDislikeCount(new JobIdentity(targetId), 1L);
        // dislike는 User 메트릭에 영향 없음
        verify(userRepository, never()).findById(any(UserIdentity.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void dislikeUp_existingDislike_throwsReactionConflictException() {
        // given
        when(reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId))
            .thenReturn(Optional.of(dislikeReactionOnJob));

        // when & then
        assertThrows(ReactionConflictException.class, () ->
            reactionWriter.dislikeUp(new UserIdentity(userId), TargetType.JOB, targetId)
        );

        verify(reactionReader).findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId);
        verify(reactionRepository, never()).save(any(Reaction.class));
        verify(userRepository, never()).findById(any(UserIdentity.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void dislikeUp_existingLike_throwsReactionConflictException() {
        // given
        when(reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId))
            .thenReturn(Optional.of(likeReactionOnJob));

        // when & then
        assertThrows(ReactionConflictException.class, () ->
            reactionWriter.dislikeUp(new UserIdentity(userId), TargetType.JOB, targetId)
        );

        verify(reactionReader).findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId);
        verify(reactionRepository, never()).save(any(Reaction.class));

        verify(jobRepository).increaseDislikeCount(eq(new JobIdentity(targetId)), eq(0L)); // Row Lock 확인
        verify(jobRepository, never()).increaseDislikeCount(eq(new JobIdentity(targetId)), eq(1L)); // 실제
        // dislike는 User 메트릭에 영향 없음
        verify(userRepository, never()).findById(any(UserIdentity.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void likeDown_existingLike_onJob_deletesReactionAndDecrementsUserLikeGivenCount() {
        // given
        User mockUser = User.newUser("google123", "test@example.com", "testuser");
        User updatedUser = mockUser.decrementLikeGivenCount();

        when(reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId))
            .thenReturn(Optional.of(likeReactionOnJob));
        doNothing().when(reactionRepository).deleteById(any(ReactionIdentity.class));
        doNothing().when(jobRepository).increaseLikeCount(any(JobIdentity.class), anyLong());
        when(userRepository.findById(any(UserIdentity.class)))
            .thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class)))
            .thenReturn(updatedUser);

        // when
        reactionWriter.likeDown(new UserIdentity(userId), TargetType.JOB, targetId);

        // then
        verify(reactionReader).findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId);
        verify(reactionRepository).deleteById(new ReactionIdentity(likeReactionOnJob.getReactionId()));
        verify(jobRepository).increaseLikeCount(new JobIdentity(targetId), -1L);
        verify(userRepository).findById(new UserIdentity(userId));
        verify(userRepository).save(any(User.class));
        verify(communityPostRepository, never()).findById(any(CommunityPostIdentity.class));
    }

    @Test
    void likeDown_existingLike_onCommunityPost_decrementsBothUserAndAuthorMetrics() {
        // given
        User mockUser = User.newUser("google123", "test@example.com", "testuser");
        User updatedUser = mockUser.decrementLikeGivenCount();

        User mockAuthor = User.newUser("google456", "author@example.com", "author");
        User updatedAuthor = mockAuthor.decrementLikesReceived();

        when(reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.COMMUNITY_POST, targetId))
            .thenReturn(Optional.of(likeReactionOnCommunityPost));
        doNothing().when(reactionRepository).deleteById(any(ReactionIdentity.class));
        doNothing().when(communityPostRepository).increaseLikeCount(any(CommunityPostIdentity.class), anyLong());
        when(userRepository.findById(new UserIdentity(userId)))
            .thenReturn(Optional.of(mockUser));
        when(userRepository.findById(new UserIdentity(authorId)))
            .thenReturn(Optional.of(mockAuthor));
        when(userRepository.save(any(User.class)))
            .thenReturn(updatedUser, updatedAuthor);
        when(communityPostRepository.findById(new CommunityPostIdentity(targetId)))
            .thenReturn(Optional.of(sampleCommunityPost));

        // when
        reactionWriter.likeDown(new UserIdentity(userId), TargetType.COMMUNITY_POST, targetId);

        // then
        verify(reactionRepository).deleteById(new ReactionIdentity(likeReactionOnCommunityPost.getReactionId()));
        verify(communityPostRepository).increaseLikeCount(new CommunityPostIdentity(targetId), -1L);
        verify(userRepository).findById(new UserIdentity(userId));
        verify(communityPostRepository).findById(new CommunityPostIdentity(targetId));
        verify(userRepository).findById(new UserIdentity(authorId));
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void likeDown_noExistingReaction_doesNothingAndNoUserUpdate() {
        // given
        when(reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId))
            .thenReturn(Optional.empty());

        // when
        reactionWriter.likeDown(new UserIdentity(userId), TargetType.JOB, targetId);

        // then
        verify(reactionReader).findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId);
        verify(reactionRepository, never()).deleteById(any(ReactionIdentity.class));
        verify(userRepository, never()).findById(any(UserIdentity.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void likeDown_existingDislike_doesNothingAndNoUserUpdate() {
        // given
        when(reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId))
            .thenReturn(Optional.of(dislikeReactionOnJob));

        // when
        reactionWriter.likeDown(new UserIdentity(userId), TargetType.JOB, targetId);

        // then
        verify(reactionReader).findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId);
        verify(reactionRepository, never()).deleteById(any(ReactionIdentity.class));
        verify(userRepository, never()).findById(any(UserIdentity.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void dislikeDown_existingDislike_deletesReactionWithoutUserMetricUpdate() {
        // given
        when(reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId))
            .thenReturn(Optional.of(dislikeReactionOnJob));
        doNothing().when(reactionRepository).deleteById(any(ReactionIdentity.class));
        doNothing().when(jobRepository).increaseDislikeCount(any(JobIdentity.class), anyLong());

        // when
        reactionWriter.dislikeDown(new UserIdentity(userId), TargetType.JOB, targetId);

        // then
        verify(reactionReader).findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId);
        verify(reactionRepository).deleteById(new ReactionIdentity(dislikeReactionOnJob.getReactionId()));
        verify(jobRepository).increaseDislikeCount(new JobIdentity(targetId), -1L);
        // dislike는 User 메트릭에 영향 없음
        verify(userRepository, never()).findById(any(UserIdentity.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void dislikeDown_noExistingReaction_doesNothingAndNoUserUpdate() {
        // given
        when(reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId))
            .thenReturn(Optional.empty());

        // when
        reactionWriter.dislikeDown(new UserIdentity(userId), TargetType.JOB, targetId);

        // then
        verify(reactionReader).findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId);
        verify(reactionRepository, never()).deleteById(any(ReactionIdentity.class));
        verify(userRepository, never()).findById(any(UserIdentity.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void dislikeDown_existingLike_doesNothingAndNoUserUpdate() {
        // given
        when(reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId))
            .thenReturn(Optional.of(likeReactionOnJob));

        // when
        reactionWriter.dislikeDown(new UserIdentity(userId), TargetType.JOB, targetId);

        // then
        verify(reactionReader).findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId);
        verify(reactionRepository, never()).deleteById(any(ReactionIdentity.class));
        verify(userRepository, never()).findById(any(UserIdentity.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void likeUp_userNotFound_throwsRuntimeException() {
        // given
        when(reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId))
            .thenReturn(Optional.empty());
        when(reactionRepository.save(any(Reaction.class)))
            .thenReturn(likeReactionOnJob);
        doNothing().when(jobRepository).increaseLikeCount(any(JobIdentity.class), anyLong());
        when(userRepository.findById(any(UserIdentity.class)))
            .thenReturn(Optional.empty()); // User not found

        // when & then
        assertThrows(RuntimeException.class, () ->
            reactionWriter.likeUp(new UserIdentity(userId), TargetType.JOB, targetId)
        );

        verify(reactionReader).findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId);
        verify(reactionRepository).save(any(Reaction.class));
        verify(jobRepository).increaseLikeCount(new JobIdentity(targetId), 1L);
        verify(userRepository).findById(new UserIdentity(userId));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void likeDown_userNotFound_throwsRuntimeException() {
        // given
        when(reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId))
            .thenReturn(Optional.of(likeReactionOnJob));
        doNothing().when(reactionRepository).deleteById(any(ReactionIdentity.class));
        doNothing().when(jobRepository).increaseLikeCount(any(JobIdentity.class), anyLong());
        when(userRepository.findById(any(UserIdentity.class)))
            .thenReturn(Optional.empty()); // User not found

        // when & then
        assertThrows(RuntimeException.class, () ->
            reactionWriter.likeDown(new UserIdentity(userId), TargetType.JOB, targetId)
        );

        verify(reactionReader).findByUserIdAndTargetTypeAndTargetId(userId, TargetType.JOB, targetId);
        verify(reactionRepository).deleteById(new ReactionIdentity(likeReactionOnJob.getReactionId()));
        verify(jobRepository).increaseLikeCount(new JobIdentity(targetId), -1L);
        verify(userRepository).findById(new UserIdentity(userId));
        verify(userRepository, never()).save(any(User.class));
    }
}
