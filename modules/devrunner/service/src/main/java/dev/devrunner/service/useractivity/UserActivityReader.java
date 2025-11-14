package dev.devrunner.service.useractivity;

import dev.devrunner.model.useractivity.UserActivity;
import dev.devrunner.model.useractivity.UserActivityIdentity;

import java.util.List;

/**
 * UserActivity 도메인 조회 서비스 인터페이스
 *
 * CQRS 패턴의 Query 책임을 담당하며,
 * Infrastructure Repository 기반으로 조회 로직을 제공합니다.
 */
public interface UserActivityReader {

    /**
     * ID로 UserActivity 조회
     *
     * @param identity UserActivity 식별자
     * @return UserActivity 엔티티
     */
    UserActivity getById(UserActivityIdentity identity);

    /**
     * 모든 UserActivity 조회
     *
     * @return UserActivity 목록
     */
    List<UserActivity> getAll();

    /**
     * 사용자 ID로 UserActivity 목록 조회
     *
     * @param userId 사용자 ID
     * @return UserActivity 목록
     */
    List<UserActivity> getByUserId(Long userId);
}
