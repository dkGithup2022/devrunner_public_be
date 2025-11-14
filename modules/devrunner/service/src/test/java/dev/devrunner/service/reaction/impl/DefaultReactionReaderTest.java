package dev.devrunner.service.reaction.impl;

import dev.devrunner.exception.reaction.ReactionNotFoundException;
import dev.devrunner.infra.reaction.repository.ReactionRepository;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.reaction.Reaction;
import dev.devrunner.model.reaction.ReactionIdentity;
import dev.devrunner.model.reaction.ReactionType;
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
class DefaultReactionReaderTest {

    @Mock
    private ReactionRepository reactionRepository;

    @InjectMocks
    private DefaultReactionReader reactionReader;

    private final Reaction sampleReaction = new Reaction(
        1L,                  // reactionId
        1L,                  // userId
        TargetType.JOB,      // targetType
        100L,                // targetId
        ReactionType.LIKE,   // reactionType
        Instant.now(),       // createdAt
        Instant.now()        // updatedAt
    );

    private final ReactionIdentity testIdentity = new ReactionIdentity(1L);

    // ========== getById 테스트 ==========

    @Test
    void getById_existingId_returnsReaction() {
        // given
        when(reactionRepository.findById(testIdentity))
            .thenReturn(Optional.of(sampleReaction));

        // when
        Reaction result = reactionReader.getById(testIdentity);

        // then
        assertNotNull(result);
        assertEquals(sampleReaction.getReactionId(), result.getReactionId());
        assertEquals(sampleReaction.getReactionType(), result.getReactionType());
        verify(reactionRepository).findById(testIdentity);
    }

    @Test
    void getById_nonExistingId_throwsReactionNotFoundException() {
        // given
        when(reactionRepository.findById(testIdentity))
            .thenReturn(Optional.empty());

        // when & then
        assertThrows(ReactionNotFoundException.class, () ->
            reactionReader.getById(testIdentity)
        );
        verify(reactionRepository).findById(testIdentity);
    }

    // ========== getAll 테스트 ==========

    @Test
    void getAll_existingReactions_returnsList() {
        // given
        List<Reaction> reactions = List.of(sampleReaction);
        when(reactionRepository.findAll())
            .thenReturn(reactions);

        // when
        List<Reaction> result = reactionReader.getAll();

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reactionRepository).findAll();
    }

    @Test
    void getAll_noReactions_returnsEmptyList() {
        // given
        when(reactionRepository.findAll())
            .thenReturn(List.of());

        // when
        List<Reaction> result = reactionReader.getAll();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reactionRepository).findAll();
    }

    // ========== getByUserId (단순) 테스트 ==========

    @Test
    void getByUserId_existingUser_returnsList() {
        // given
        Long userId = 1L;
        List<Reaction> reactions = List.of(sampleReaction);
        when(reactionRepository.findByUserId(userId))
            .thenReturn(reactions);

        // when
        List<Reaction> result = reactionReader.getByUserId(userId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        verify(reactionRepository).findByUserId(userId);
    }

    @Test
    void getByUserId_nonExistingUser_returnsEmptyList() {
        // given
        Long userId = 999L;
        when(reactionRepository.findByUserId(userId))
            .thenReturn(List.of());

        // when
        List<Reaction> result = reactionReader.getByUserId(userId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reactionRepository).findByUserId(userId);
    }

    // ========== getByUserId (타입, 페이징) 테스트 ==========

    @Test
    void getByUserIdAndType_existingData_returnsList() {
        // given
        Long userId = 1L;
        ReactionType type = ReactionType.LIKE;
        int page = 0;
        int size = 20;
        List<Reaction> reactions = List.of(sampleReaction);
        when(reactionRepository.findByUserIdAndType(userId, type, page, size))
            .thenReturn(reactions);

        // when
        List<Reaction> result = reactionReader.getByUserId(userId, type, page, size);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(type, result.get(0).getReactionType());
        verify(reactionRepository).findByUserIdAndType(userId, type, page, size);
    }

    @Test
    void getByUserIdAndType_nonExistingData_returnsEmptyList() {
        // given
        Long userId = 999L;
        ReactionType type = ReactionType.DISLIKE;
        int page = 0;
        int size = 20;
        when(reactionRepository.findByUserIdAndType(userId, type, page, size))
            .thenReturn(List.of());

        // when
        List<Reaction> result = reactionReader.getByUserId(userId, type, page, size);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reactionRepository).findByUserIdAndType(userId, type, page, size);
    }

    // ========== getByTargetTypeAndTargetId 테스트 ==========

    @Test
    void getByTargetTypeAndTargetId_existingData_returnsList() {
        // given
        TargetType targetType = TargetType.JOB;
        Long targetId = 100L;
        List<Reaction> reactions = List.of(sampleReaction);
        when(reactionRepository.findByTargetTypeAndTargetId(targetType, targetId))
            .thenReturn(reactions);

        // when
        List<Reaction> result = reactionReader.getByTargetTypeAndTargetId(targetType, targetId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(targetType, result.get(0).getTargetType());
        assertEquals(targetId, result.get(0).getTargetId());
        verify(reactionRepository).findByTargetTypeAndTargetId(targetType, targetId);
    }

    @Test
    void getByTargetTypeAndTargetId_nonExistingData_returnsEmptyList() {
        // given
        TargetType targetType = TargetType.TECH_BLOG;
        Long targetId = 999L;
        when(reactionRepository.findByTargetTypeAndTargetId(targetType, targetId))
            .thenReturn(List.of());

        // when
        List<Reaction> result = reactionReader.getByTargetTypeAndTargetId(targetType, targetId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reactionRepository).findByTargetTypeAndTargetId(targetType, targetId);
    }

    // ========== findByUserIdAndTargetTypeAndTargetId 테스트 ==========

    @Test
    void findByUserIdAndTargetTypeAndTargetId_existingReaction_returnsOptionalWithReaction() {
        // given
        Long userId = 1L;
        TargetType targetType = TargetType.JOB;
        Long targetId = 100L;
        when(reactionRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId))
            .thenReturn(Optional.of(sampleReaction));

        // when
        Optional<Reaction> result = reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);

        // then
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getUserId());
        assertEquals(targetType, result.get().getTargetType());
        assertEquals(targetId, result.get().getTargetId());
        verify(reactionRepository).findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
    }

    @Test
    void findByUserIdAndTargetTypeAndTargetId_nonExistingReaction_returnsEmptyOptional() {
        // given
        Long userId = 999L;
        TargetType targetType = TargetType.JOB;
        Long targetId = 100L;
        when(reactionRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId))
            .thenReturn(Optional.empty());

        // when
        Optional<Reaction> result = reactionReader.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);

        // then
        assertFalse(result.isPresent());
        verify(reactionRepository).findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
    }
}
