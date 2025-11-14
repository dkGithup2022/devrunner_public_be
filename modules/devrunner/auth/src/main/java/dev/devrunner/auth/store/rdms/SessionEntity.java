package dev.devrunner.auth.store.rdms;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/**
 * 로그인 세션 엔티티
 *
 * Persistable 구현:
 * - Spring Data JDBC는 @Id 필드가 null이 아니면 UPDATE를 시도함
 * - 세션은 수동으로 생성한 sessionKey를 사용하므로, Persistable을 구현하여
 *   isNew()를 통해 항상 INSERT하도록 명시적으로 지정
 */
@Table("login_sessions")
@Getter
@AllArgsConstructor
public class SessionEntity implements Persistable<String> {
    @Id
    private String sessionKey;
    private Long userId;
    private Instant createdAt;
    private Instant expiresAt;

    public static SessionEntity newOne(String key, Long userId, Instant createdAt, Instant expiresAt) {
        return new SessionEntity(key, userId, createdAt, expiresAt);
    }

    @Override
    public String getId() {
        return sessionKey;
    }

    /**
     * 항상 새 엔티티로 처리 (INSERT)
     * 세션은 생성만 하고 업데이트하지 않으므로 항상 true 반환
     */
    @Override
    public boolean isNew() {
        return true;
    }
}
