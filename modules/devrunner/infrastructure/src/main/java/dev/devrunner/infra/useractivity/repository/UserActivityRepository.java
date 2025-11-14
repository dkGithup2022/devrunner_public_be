package dev.devrunner.infra.useractivity.repository;

import dev.devrunner.model.useractivity.UserActivity;
import dev.devrunner.model.useractivity.UserActivityIdentity;
import java.util.List;
import java.util.Optional;

/**
 * UserActivity Repository 인터페이스
 *
 * 헥사고날 아키텍처에서 Port 역할을 수행하며,
 * 사용자 활동 도메인의 데이터 접근을 위한 인터페이스를 정의합니다.
 */
public interface UserActivityRepository {

    /**
     * ID로 UserActivity 조회
     *
     * @param identity UserActivity 식별자
     * @return UserActivity 엔티티 (존재하지 않으면 Optional.empty())
     */
    Optional<UserActivity> findById(UserActivityIdentity identity);

    /**
     * UserActivity 저장 (생성/수정)
     *
     * @param userActivity 저장할 UserActivity
     * @return 저장된 UserActivity
     */
    UserActivity save(UserActivity userActivity);

    /**
     * ID로 UserActivity 삭제
     *
     * @param identity UserActivity 식별자
     */
    void deleteById(UserActivityIdentity identity);

    /**
     * UserActivity 존재 여부 확인
     *
     * @param identity UserActivity 식별자
     * @return 존재하면 true, 없으면 false
     */
    boolean existsById(UserActivityIdentity identity);

    /**
     * 모든 UserActivity 조회
     *
     * @return UserActivity 목록
     */
    List<UserActivity> findAll();

    /**
     * 사용자 ID로 UserActivity 목록 조회
     *
     * @param userId 사용자 ID
     * @return UserActivity 목록
     */
    List<UserActivity> findByUserId(Long userId);
}
