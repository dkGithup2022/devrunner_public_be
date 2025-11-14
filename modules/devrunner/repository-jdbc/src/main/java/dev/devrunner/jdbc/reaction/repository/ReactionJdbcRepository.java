package dev.devrunner.jdbc.reaction.repository;

import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.reaction.Reaction;
import dev.devrunner.model.reaction.ReactionIdentity;
import dev.devrunner.model.reaction.ReactionType;
import dev.devrunner.infra.reaction.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Reaction Repository 구현체
 *
 * 헥사고날 아키텍처에서 Adapter 역할을 수행하며,
 * Infrastructure의 ReactionRepository 인터페이스를
 * Spring Data JDBC를 활용하여 구현합니다.
 */
@Repository
@RequiredArgsConstructor
public class ReactionJdbcRepository implements ReactionRepository {

    private final ReactionEntityRepository entityRepository;

    @Override
    public Optional<Reaction> findById(ReactionIdentity identity) {
        return entityRepository.findById(identity.getReactionId())
                .map(this::toDomain);
    }

    @Override
    public Reaction save(Reaction reaction) {
        ReactionEntity entity = toEntity(reaction);
        ReactionEntity saved = entityRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(ReactionIdentity identity) {
        entityRepository.deleteById(identity.getReactionId());
    }

    @Override
    public boolean existsById(ReactionIdentity identity) {
        return entityRepository.existsById(identity.getReactionId());
    }

    @Override
    public List<Reaction> findAll() {
        return StreamSupport.stream(entityRepository.findAll().spliterator(), false)
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reaction> findByUserId(Long userId) {
        return entityRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reaction> findByTargetTypeAndTargetId(TargetType targetType, Long targetId) {
        return entityRepository.findByTargetTypeAndTargetId(targetType, targetId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Reaction> findByUserIdAndTargetTypeAndTargetId(Long userId, TargetType targetType, Long targetId) {
        return entityRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)
                .map(this::toDomain);
    }

    @Override
    public long countByTargetTypeAndTargetIdAndReactionType(TargetType targetType, Long targetId, ReactionType reactionType) {
        return entityRepository.countByTargetTypeAndTargetIdAndReactionType(targetType, targetId, reactionType);
    }

    @Override
    public List<Reaction> findByUserIdAndType(Long userId, ReactionType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return entityRepository.findByUserIdAndReactionType(userId, type, pageable).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Entity ↔ Domain 변환 메서드
     */
    private Reaction toDomain(ReactionEntity entity) {
        return new Reaction(
                entity.getId(),
                entity.getUserId(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getReactionType(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private ReactionEntity toEntity(Reaction domain) {
        return new ReactionEntity(
                domain.getReactionId(),
                domain.getUserId(),
                domain.getTargetType(),
                domain.getTargetId(),
                domain.getReactionType(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
