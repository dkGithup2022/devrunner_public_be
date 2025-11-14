package dev.devrunner.model.bookmark;

import dev.devrunner.model.AuditProps;
import dev.devrunner.model.common.TargetType;
import lombok.Value;
import java.time.Instant;

/**
 * 북마크 도메인 모델
 */
@Value
public class Bookmark implements AuditProps {
    Long bookmarkId;
    Long userId;
    TargetType targetType;
    Long targetId;
    Instant createdAt;
    Instant updatedAt;

    /**
     * 새로운 북마크 생성
     *
     * @param userId 사용자 ID
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     * @return 생성된 북마크 객체
     */
    public static Bookmark create(
        Long userId,
        TargetType targetType,
        Long targetId
    ) {
        Instant now = Instant.now();
        return new Bookmark(
            null,
            userId,
            targetType,
            targetId,
            now,
            now
        );
    }
}
