package dev.devrunner.jdbc.comment.repository;

import dev.devrunner.model.common.CommentOrder;
import dev.devrunner.model.common.TargetType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import javax.annotation.processing.Generated;
import java.time.Instant;

/**
 * Comment Spring Data JDBC Entity
 *
 * Model 클래스 스펙을 기반으로 생성된 데이터베이스 매핑용 엔티티
 * - CommentOrder: @Embedded로 comments 테이블에 컬럼으로 저장
 */
@Table("comments")
@Getter
@AllArgsConstructor
public class CommentEntity {
    @Id
    private Long id;
    private Long userId;
    private String content;
    private TargetType targetType;
    private Long targetId;
    private Long parentId;

    // Embedded - comments 테이블에 컬럼으로 flatten
    @Embedded.Nullable
    private CommentOrder commentOrder;

    private Boolean isHidden;
    private Instant createdAt;
    private Instant updatedAt;
}
