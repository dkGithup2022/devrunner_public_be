package dev.devrunner.service.auth.impl;

import dev.devrunner.service.auth.dto.UserInfo;
import dev.devrunner.auth.model.SessionUser;
import dev.devrunner.auth.store.SessionStore;
import dev.devrunner.exception.auth.UnauthorizedException;
import dev.devrunner.exception.user.UserNotFoundException;
import dev.devrunner.model.user.User;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.service.auth.SessionService;
import dev.devrunner.service.auth.client.GoogleOAuthClient;
import dev.devrunner.service.auth.client.dto.GoogleUserInfo;
import dev.devrunner.service.user.UserReader;
import dev.devrunner.service.user.UserWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * 세션 관리 서비스 구현체
 *
 * OAuth 기반 로그인/로그아웃 및 세션 기반 사용자 정보 조회를 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultSessionService implements SessionService {

    private final UserReader userReader;
    private final UserWriter userWriter;
    private final SessionStore sessionStore;
    private final GoogleOAuthClient googleOAuthClient;

    private static final Duration SESSION_TTL = Duration.ofDays(3);  // 3일

    @Override
    @Transactional
    public String login(String accessToken) {
        // 1. Google API 호출하여 사용자 정보 가져오기
        GoogleUserInfo googleUserInfo = googleOAuthClient.getUserInfo(accessToken);

        // 2. Google ID로 사용자 조회
        User user = userReader.findByGoogleId(googleUserInfo.getGoogleId())
                .orElseThrow(() -> new UserNotFoundException("User not found. Please sign up first."));

        // 3. 탈퇴한 회원 로그인 차단
        if (user.getIsWithdrawn()) {
            throw new UnauthorizedException("Withdrawn user. Please sign up again.");
        }

        // 4. 마지막 로그인 시간 업데이트
        User updatedUser = user.updateLastLogin();
        userWriter.upsert(updatedUser);

        // 5. 세션 생성
        SessionUser sessionUser = SessionUser.of(
                updatedUser.getUserId(),
                updatedUser.getLastLoginAt(),
                updatedUser.getLastLoginAt().plus(SESSION_TTL)
        );

        String sessionId = sessionStore.createSession(sessionUser, SESSION_TTL);

        log.info("User logged in: userId={}, email={}", updatedUser.getUserId(), updatedUser.getEmail());

        return sessionId;
    }

    @Override
    public void logout(String sessionId) {
        if (sessionId != null) {
            sessionStore.deleteSession(sessionId);
            log.info("User logged out: sessionId={}", sessionId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfo getCurrentUser(SessionUser sessionUser) {
        // DB에서 최신 사용자 정보 조회
        if (sessionUser == null || sessionUser.getUserId() == null) {
            throw new UnauthorizedException("User not found. Please sign up first.");
        }
        User user = userReader.findById(new UserIdentity(sessionUser.getUserId()))
                .orElseThrow(() -> new UserNotFoundException("user not found - id : " + sessionUser.getUserId()));

        return UserInfo.of(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                null,  // picture 필드가 User 모델에 없음
                user.getGoogleId()
        );
    }
}
