package dev.devrunner.infra.notification.repository;

import dev.devrunner.model.notification.Notification;
import dev.devrunner.model.notification.NotificationIdentity;
import java.util.List;
import java.util.Optional;

/**
 * Notification Repository 인터페이스
 *
 * 헥사고날 아키텍처에서 Port 역할을 수행하며,
 * 알림 도메인의 데이터 접근을 위한 인터페이스를 정의합니다.
 */
public interface NotificationRepository {

    /**
     * ID로 Notification 조회
     *
     * @param identity Notification 식별자
     * @return Notification 엔티티 (존재하지 않으면 Optional.empty())
     */
    Optional<Notification> findById(NotificationIdentity identity);

    /**
     * Notification 저장 (생성/수정)
     *
     * @param notification 저장할 Notification
     * @return 저장된 Notification
     */
    Notification save(Notification notification);

    /**
     * ID로 Notification 삭제
     *
     * @param identity Notification 식별자
     */
    void deleteById(NotificationIdentity identity);

    /**
     * Notification 존재 여부 확인
     *
     * @param identity Notification 식별자
     * @return 존재하면 true, 없으면 false
     */
    boolean existsById(NotificationIdentity identity);

    /**
     * 모든 Notification 조회
     *
     * @return Notification 목록
     */
    List<Notification> findAll();

    /**
     * 사용자 ID로 Notification 목록 조회
     *
     * @param userId 사용자 ID
     * @return Notification 목록
     */
    List<Notification> findByUserId(Long userId);
}
