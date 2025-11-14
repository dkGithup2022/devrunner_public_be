package dev.devrunner.infra.bookmark.repository;

import dev.devrunner.model.bookmark.Bookmark;
import dev.devrunner.model.bookmark.BookmarkIdentity;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.user.UserIdentity;

import java.util.List;
import java.util.Optional;

/**
 * Bookmark Repository 인터페이스
 *
 * 헥사고날 아키텍처에서 Port 역할을 수행하며,
 * 북마크 도메인의 데이터 접근을 위한 인터페이스를 정의합니다.
 */
public interface BookmarkRepository {

    // ============ 기본 CRUD ============

    /**
     * ID로 Bookmark 조회
     *
     * @param identity Bookmark 식별자
     * @return Bookmark 엔티티 (존재하지 않으면 Optional.empty())
     */
    Optional<Bookmark> findById(BookmarkIdentity identity);

    /**
     * Bookmark 저장 (생성/수정)
     *
     * @param bookmark 저장할 Bookmark
     * @return 저장된 Bookmark
     */
    Bookmark save(Bookmark bookmark);

    /**
     * ID로 Bookmark 삭제
     *
     * @param identity Bookmark 식별자
     */
    void deleteById(BookmarkIdentity identity);

    /**
     * Bookmark 존재 여부 확인
     *
     * @param identity Bookmark 식별자
     * @return 존재하면 true, 없으면 false
     */
    boolean existsById(BookmarkIdentity identity);

    // ============ 비즈니스 조회 ============

    /**
     * 사용자 ID로 Bookmark 목록 조회 (페이지네이션)
     *
     * @param userIdentity 사용자 식별자
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @return Bookmark 목록
     */
    List<Bookmark> findByUserId(UserIdentity userIdentity, int page, int size);

    /**
     * 사용자 ID + TargetType 필터로 Bookmark 목록 조회 (페이지네이션)
     *
     * @param userIdentity 사용자 식별자
     * @param targetType 대상 타입
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @return Bookmark 목록
     */
    List<Bookmark> findByUserIdAndTargetType(UserIdentity userIdentity, TargetType targetType, int page, int size);

    /**
     * 사용자 ID로 Bookmark 총 개수 조회
     * (페이지네이션의 totalCount 계산용)
     *
     * @param userIdentity 사용자 식별자
     * @return Bookmark 개수
     */
    long countByUserId(UserIdentity userIdentity);

    /**
     * 사용자 ID, 대상 타입, 대상 ID로 Bookmark 조회
     * ⭐ 가장 중요! (북마크 여부 확인 & 삭제 시 사용)
     *
     * @param userIdentity 사용자 식별자
     * @param targetType 대상 타입 (JOB, COMMUNITY_POST, TECH_BLOG 등)
     * @param targetId 대상 ID
     * @return Bookmark 엔티티 (존재하지 않으면 Optional.empty())
     */
    Optional<Bookmark> findByUserIdAndTargetTypeAndTargetId(
        UserIdentity userIdentity,
        TargetType targetType,
        Long targetId
    );
}
