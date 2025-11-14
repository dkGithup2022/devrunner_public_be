package dev.devrunner.jdbc.reaction.repository;

import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.reaction.Reaction;
import dev.devrunner.model.reaction.ReactionIdentity;
import dev.devrunner.model.reaction.ReactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReactionJdbcRepository 테스트
 *
 * Entity↔Domain 변환 로직 검증
 */
@DataJdbcTest
@ComponentScan("dev.devrunner.jdbc.reaction.repository")
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReactionJdbcRepositoryTest {

    @Autowired
    private ReactionJdbcRepository reactionRepository;

    // ========== Entity↔Domain 변환 테스트 ==========

    @Test
    void save_withValidDomain_returnsConvertedDomain() {
        // given
        Reaction reaction = Reaction.create(
                1L,
                TargetType.JOB,
                100L,
                ReactionType.LIKE
        );

        // when
        Reaction saved = reactionRepository.save(reaction);

        // then - ID 생성 및 주요 필드 변환 확인
        assertThat(saved).isNotNull();
        assertThat(saved.getReactionId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getTargetType()).isEqualTo(TargetType.JOB);
        assertThat(saved.getTargetId()).isEqualTo(100L);
        assertThat(saved.getReactionType()).isEqualTo(ReactionType.LIKE);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findById_existingId_returnsConvertedDomain() {
        // given
        Reaction reaction = Reaction.create(
                2L,
                TargetType.TECH_BLOG,
                200L,
                ReactionType.DISLIKE
        );
        Reaction saved = reactionRepository.save(reaction);
        ReactionIdentity identity = new ReactionIdentity(saved.getReactionId());

        // when
        Optional<Reaction> found = reactionRepository.findById(identity);

        // then - Entity → Domain 변환 확인
        assertThat(found).isPresent();
        assertThat(found.get().getReactionId()).isEqualTo(saved.getReactionId());
        assertThat(found.get().getUserId()).isEqualTo(2L);
        assertThat(found.get().getTargetType()).isEqualTo(TargetType.TECH_BLOG);
        assertThat(found.get().getTargetId()).isEqualTo(200L);
        assertThat(found.get().getReactionType()).isEqualTo(ReactionType.DISLIKE);
    }
}
