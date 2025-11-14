package dev.devrunner.jdbc.reaction.repository;

import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.reaction.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Reaction Spring Data JDBC Entity
 *
 * Model 클래스 스펙을 기반으로 생성된 데이터베이스 매핑용 엔티티
 */
@Table("reactions")
@Getter
@AllArgsConstructor
public class ReactionEntity {
    @Id
    private Long id;
    private Long userId;
    private TargetType targetType;
    private Long targetId;
    private ReactionType reactionType;
    private Instant createdAt;
    private Instant updatedAt;
}
