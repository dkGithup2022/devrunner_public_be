package dev.devrunner.service.auth;

import dev.devrunner.service.auth.dto.NicknameCheckResult;
import dev.devrunner.service.auth.dto.UserCheckResult;

/**
 * 사용자 온보딩 서비스 인터페이스
 *
 * 회원가입 및 가입 전 검증 로직을 담당합니다.
 */
public interface UserOnboardingService {

    /**
     * 회원가입
     *
     * @param accessToken Google Access Token
     * @param nickname    사용자 닉네임
     */
    void signup(String accessToken, String nickname);

    /**
     * 사용자 존재 여부 확인 (이메일 기반)
     *
     * @param accessToken Google Access Token
     * @return 사용자 존재 여부 및 이메일
     */
    UserCheckResult checkUser(String accessToken);

    /**
     * 닉네임 사용 가능 여부 확인
     *
     * @param nickname 확인할 닉네임
     * @return 사용 가능 여부
     */
    NicknameCheckResult checkNickname(String nickname);
}
