package dev.devrunner.auth.store.rdms;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SessionEntityRepository extends CrudRepository<SessionEntity, String> {

    /**
     * 만료된 세션 삭제
     *
     * @param expiresAt 기준 시간 (이 시간 이전에 만료된 세션 삭제)
     * @return 삭제된 세션 수
     */
    long deleteByExpiresAtBefore(Instant expiresAt);

    /**
     * 사용자 ID로 세션 조회
     *
     * @param userId 사용자 ID
     * @return 해당 사용자의 모든 세션
     */
    List<SessionEntity> findByUserId(Long userId);

    /**
     * 사용자 ID로 모든 세션 삭제 (Single Session Policy)
     *
     * 단일 DELETE 쿼리로 실행하여 성능 최적화:
     * - Spring Data JDBC의 기본 deleteBy*는 SELECT 후 개별 DELETE를 반복 (N+1 문제)
     * - @Query를 사용하여 한 번의 DELETE 쿼리로 처리
     *
     * @param userId 사용자 ID
     * @return 삭제된 세션 수
     */
    @Modifying
    @Query("DELETE FROM login_sessions WHERE user_id = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
