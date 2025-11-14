package dev.devrunner.jdbc.bookmark.repository;

import dev.devrunner.model.common.TargetType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Bookmark Entity CRUD API 인터페이스
 *
 * Spring Data JDBC를 활용한 BookmarkEntity 데이터 접근 계층
 * Infrastructure Repository 인터페이스 기반으로 필요한 메서드만 생성
 */
@Repository
public interface BookmarkEntityRepository extends CrudRepository<BookmarkEntity, Long> {

    /**
     * 사용자 ID로 Bookmark 목록 조회 (페이지네이션)
     */
    List<BookmarkEntity> findByUserId(Long userId, Pageable pageable);

    /**
     * 사용자 ID + TargetType으로 Bookmark 목록 조회 (페이지네이션)
     */
    List<BookmarkEntity> findByUserIdAndTargetType(Long userId, TargetType targetType, Pageable pageable);

    /**
     * 사용자 ID로 Bookmark 개수 조회
     */
    long countByUserId(Long userId);

    /**
     * 사용자 ID, 대상 타입, 대상 ID로 Bookmark 조회
     */
    Optional<BookmarkEntity> findByUserIdAndTargetTypeAndTargetId(Long userId, TargetType targetType, Long targetId);
}
