package dev.devrunner.service.useractivity;

import dev.devrunner.model.useractivity.UserActivity;
import dev.devrunner.model.useractivity.UserActivityIdentity;

/**
 * UserActivity 도메인 변경 서비스 인터페이스
 *
 * CQRS 패턴의 Command 책임을 담당하며,
 * Infrastructure Repository 기반으로 변경 로직을 제공합니다.
 */
public interface UserActivityWriter {

    /**
     * UserActivity 저장 (생성/수정)
     *
     * @param userActivity 저장할 UserActivity
     * @return 저장된 UserActivity
     */
    UserActivity upsert(UserActivity userActivity);

    /**
     * ID로 UserActivity 삭제
     *
     * @param identity UserActivity 식별자
     */
    void delete(UserActivityIdentity identity);
}
