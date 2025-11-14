package dev.devrunner.service.auth;

import dev.devrunner.service.auth.dto.UserInfo;
import dev.devrunner.auth.model.SessionUser;

/**
 * 세션 관리 서비스 인터페이스
 *
 * 로그인, 로그아웃 및 현재 사용자 정보 조회를 담당합니다.
 */
public interface SessionService {

    /**
     * 로그인
     *
     * @param accessToken Google Access Token
     * @return 세션 ID
     */
    String login(String accessToken);

    /**
     * 로그아웃
     *
     * @param sessionId 세션 ID
     */
    void logout(String sessionId);

    /**
     * 현재 로그인된 사용자 정보 조회
     *
     * @param sessionUser 인증된 세션 사용자
     * @return 사용자 정보
     */
    UserInfo getCurrentUser(SessionUser sessionUser);
}
