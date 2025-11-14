package dev.devrunner.service.reaction;

import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.reaction.Reaction;
import dev.devrunner.model.reaction.ReactionIdentity;
import dev.devrunner.model.reaction.ReactionType;

import java.util.List;
import java.util.Optional;

/**
 * Reaction 도메인 조회 서비스 인터페이스
 *
 * CQRS 패턴의 Query 책임을 담당하며,
 * Infrastructure Repository 기반으로 조회 로직을 제공합니다.
 */
public interface ReactionReader {

    /**
     * ID로 Reaction 조회
     *
     * @param identity Reaction 식별자
     * @return Reaction 엔티티
     */
    Reaction getById(ReactionIdentity identity);

    /**
     * 모든 Reaction 조회
     *
     * @return Reaction 목록
     */
    List<Reaction> getAll();

    /**
     * 사용자 ID로 Reaction 목록 조회
     *
     * @param userId 사용자 ID
     * @return Reaction 목록
     */
    List<Reaction> getByUserId(Long userId);

    /**
     * 사용자 ID로 Reaction 목록 조회 (페이징 및 타입 필터링)
     *
     * @param userId 사용자 ID
     * @param type 반응 타입 (LIKE, DISLIKE)
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @return Reaction 목록
     */
    List<Reaction> getByUserId(Long userId, ReactionType type, int page, int size);

    /**
     * 대상 타입과 대상 ID로 Reaction 목록 조회
     *
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     * @return Reaction 목록
     */
    List<Reaction> getByTargetTypeAndTargetId(TargetType targetType, Long targetId);

    /**
     * 사용자 ID, 대상 타입, 대상 ID로 Reaction 조회 (중복 반응 체크용)
     *
     * @param userId 사용자 ID
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     * @return Reaction 엔티티 (존재하지 않으면 Optional.empty())
     */
    Optional<Reaction> findByUserIdAndTargetTypeAndTargetId(Long userId, TargetType targetType, Long targetId);
}
