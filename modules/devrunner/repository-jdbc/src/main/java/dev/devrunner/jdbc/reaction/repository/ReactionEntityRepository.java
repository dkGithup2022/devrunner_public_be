package dev.devrunner.jdbc.reaction.repository;

import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.reaction.ReactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Reaction Entity CRUD API 인터페이스
 *
 * Spring Data JDBC를 활용한 ReactionEntity 데이터 접근 계층
 * Infrastructure Repository 인터페이스 기반으로 필요한 메서드만 생성
 */
@Repository
public interface ReactionEntityRepository extends CrudRepository<ReactionEntity, Long> {
    List<ReactionEntity> findByUserId(Long userId);
    List<ReactionEntity> findByUserIdAndReactionType(Long userId, ReactionType reactionType, Pageable pageable);
    List<ReactionEntity> findByTargetTypeAndTargetId(TargetType targetType, Long targetId);
    Optional<ReactionEntity> findByUserIdAndTargetTypeAndTargetId(Long userId, TargetType targetType, Long targetId);
    long countByTargetTypeAndTargetIdAndReactionType(TargetType targetType, Long targetId, ReactionType reactionType);
}
