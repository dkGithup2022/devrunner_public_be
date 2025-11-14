package dev.devrunner.service.bookmark;

import dev.devrunner.exception.bookmark.BookmarkNotFoundException;
import dev.devrunner.model.bookmark.Bookmark;
import dev.devrunner.model.bookmark.BookmarkIdentity;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.user.UserIdentity;

import java.util.List;
import java.util.Optional;

/**
 * Bookmark 도메인 조회 서비스 인터페이스
 *
 * CQRS 패턴의 Query 책임을 담당하며,
 * Infrastructure Repository 기반으로 조회 로직을 제공합니다.
 */
public interface BookmarkReader {

    /**
     * ID로 Bookmark 조회
     *
     * @param identity Bookmark 식별자
     * @return Bookmark 엔티티
     * @throws BookmarkNotFoundException Bookmark를 찾을 수 없는 경우
     */
    Bookmark getById(BookmarkIdentity identity);

    /**
     * 사용자 ID로 Bookmark 목록 조회 (페이지네이션)
     *
     * @param userIdentity 사용자 식별자
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @return Bookmark 목록
     */
    List<Bookmark> getByUserId(UserIdentity userIdentity, int page, int size);

    /**
     * 사용자 ID + TargetType으로 Bookmark 목록 조회 (페이지네이션)
     *
     * @param userIdentity 사용자 식별자
     * @param targetType 대상 타입
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @return Bookmark 목록
     */
    List<Bookmark> getByUserIdAndTargetType(UserIdentity userIdentity, TargetType targetType, int page, int size);

    /**
     * 사용자 ID로 Bookmark 총 개수 조회
     *
     * @param userIdentity 사용자 식별자
     * @return Bookmark 개수
     */
    long countByUserId(UserIdentity userIdentity);

    /**
     * 사용자 ID, 대상 타입, 대상 ID로 Bookmark 조회 (북마크 여부 확인용)
     *
     * @param userIdentity 사용자 식별자
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     * @return Bookmark 엔티티 (존재하지 않으면 Optional.empty())
     */
    Optional<Bookmark> findByUserIdAndTargetTypeAndTargetId(UserIdentity userIdentity, TargetType targetType, Long targetId);
}
