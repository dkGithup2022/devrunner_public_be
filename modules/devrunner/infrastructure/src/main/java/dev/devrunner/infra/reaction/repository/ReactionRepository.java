package dev.devrunner.infra.reaction.repository;

import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.reaction.Reaction;
import dev.devrunner.model.reaction.ReactionIdentity;
import dev.devrunner.model.reaction.ReactionType;
import java.util.List;
import java.util.Optional;

/**
 * Reaction Repository 인터페이스
 *
 * 헥사고날 아키텍처에서 Port 역할을 수행하며,
 * 반응(좋아요/싫어요) 도메인의 데이터 접근을 위한 인터페이스를 정의합니다.
 */
public interface ReactionRepository {

    /**
     * ID로 Reaction 조회
     *
     * @param identity Reaction 식별자
     * @return Reaction 엔티티 (존재하지 않으면 Optional.empty())
     */
    Optional<Reaction> findById(ReactionIdentity identity);

    /**
     * Reaction 저장 (생성/수정)
     *
     * @param reaction 저장할 Reaction
     * @return 저장된 Reaction
     */
    Reaction save(Reaction reaction);

    /**
     * ID로 Reaction 삭제
     *
     * @param identity Reaction 식별자
     */
    void deleteById(ReactionIdentity identity);

    /**
     * Reaction 존재 여부 확인
     *
     * @param identity Reaction 식별자
     * @return 존재하면 true, 없으면 false
     */
    boolean existsById(ReactionIdentity identity);

    /**
     * 모든 Reaction 조회
     *
     * @return Reaction 목록
     */
    List<Reaction> findAll();

    /**
     * 사용자 ID로 Reaction 목록 조회
     *
     * @param userId 사용자 ID
     * @return Reaction 목록
     */
    List<Reaction> findByUserId(Long userId);

    /**
     * 대상 타입과 대상 ID로 Reaction 목록 조회
     *
     * @param targetType 대상 타입 (JOB, COMMUNITY_POST, TECH_BLOG 등)
     * @param targetId 대상 ID
     * @return Reaction 목록
     */
    List<Reaction> findByTargetTypeAndTargetId(TargetType targetType, Long targetId);

    /**
     * 사용자 ID, 대상 타입, 대상 ID로 Reaction 조회
     *
     * @param userId 사용자 ID
     * @param targetType 대상 타입 (JOB, COMMUNITY_POST, TECH_BLOG 등)
     * @param targetId 대상 ID
     * @return Reaction 엔티티 (존재하지 않으면 Optional.empty())
     */
    Optional<Reaction> findByUserIdAndTargetTypeAndTargetId(Long userId, TargetType targetType, Long targetId);

    /**
     * 대상 타입, 대상 ID, 반응 타입으로 Reaction 개수 조회
     *
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     * @param reactionType 반응 타입 (LIKE, DISLIKE)
     * @return Reaction 개수
     */
    long countByTargetTypeAndTargetIdAndReactionType(TargetType targetType, Long targetId, ReactionType reactionType);

    /**
     * 사용자 ID와 반응 타입으로 Reaction 목록 조회 (페이징)
     *
     * @param userId 사용자 ID
     * @param type 반응 타입 (LIKE, DISLIKE)
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @return Reaction 목록
     */
    List<Reaction> findByUserIdAndType(Long userId, ReactionType type, int page, int size);
}
