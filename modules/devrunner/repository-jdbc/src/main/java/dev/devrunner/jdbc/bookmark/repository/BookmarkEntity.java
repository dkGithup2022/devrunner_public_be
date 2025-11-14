package dev.devrunner.jdbc.bookmark.repository;

import dev.devrunner.model.common.TargetType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Bookmark Spring Data JDBC Entity
 *
 * Model 클래스 스펙을 기반으로 생성된 데이터베이스 매핑용 엔티티
 */
@Table("bookmarks")
@Getter
@AllArgsConstructor
public class BookmarkEntity {
    @Id
    private Long id;
    private Long userId;
    private TargetType targetType;
    private Long targetId;
    private Instant createdAt;
    private Instant updatedAt;
}
