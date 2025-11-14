package dev.devrunner.service.reaction;

import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.user.UserIdentity;

/**
 * Reaction 서비스 인터페이스
 *
 * 좋아요/싫어요 반응을 처리하는 비즈니스 로직을 제공합니다.
 */
public interface ReactionWriter {

    /**
     * 좋아요 추가
     *
     * @param user 사용자 Identity
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     */
    void likeUp(UserIdentity user, TargetType targetType, Long targetId);

    /**
     * 싫어요 추가
     *
     * @param user 사용자 Identity
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     */
    void dislikeUp(UserIdentity user, TargetType targetType, Long targetId);

    /**
     * 좋아요 취소
     *
     * @param user 사용자 Identity
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     */
    void likeDown(UserIdentity user, TargetType targetType, Long targetId);

    /**
     * 싫어요 취소
     *
     * @param user 사용자 Identity
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     */
    void dislikeDown(UserIdentity user, TargetType targetType, Long targetId);
}
