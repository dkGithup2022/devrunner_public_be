package dev.devrunner.service.user;

import dev.devrunner.model.user.User;
import dev.devrunner.model.user.UserIdentity;

import java.util.List;
import java.util.Optional;

/**
 * User 도메인 조회 서비스 인터페이스
 *
 * CQRS 패턴의 Query 책임을 담당하며,
 * Infrastructure Repository 기반으로 조회 로직을 제공합니다.
 */
public interface UserReader {

    /**
     * ID로 User 조회
     *
     * @param identity User 식별자
     * @return User 엔티티 (Optional)
     */
    Optional<User> findById(UserIdentity identity);

    /**
     * 모든 User 조회
     *
     * @return User 목록
     */
    List<User> findAll();

    /**
     * Google ID로 User 조회
     *
     * @param googleId Google 식별자
     * @return User 엔티티 (Optional)
     */
    Optional<User> findByGoogleId(String googleId);

    /**
     * 이메일로 User 조회
     *
     * @param email 이메일 주소
     * @return User 엔티티 (Optional)
     */
    Optional<User> findByEmail(String email);

    /**
     * 닉네임으로 User 조회
     *
     * @param nickname 사용자 닉네임
     * @return User 엔티티 (Optional)
     */
    Optional<User> findByNickname(String nickname);
}
