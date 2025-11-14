package dev.devrunner.service.auth.impl;

import dev.devrunner.exception.user.DuplicateUserException;
import dev.devrunner.exception.user.InvalidNicknameFormatException;
import dev.devrunner.service.auth.dto.NicknameCheckResult;
import dev.devrunner.service.auth.dto.UserCheckResult;
import dev.devrunner.model.user.User;
import dev.devrunner.service.auth.UserOnboardingService;
import dev.devrunner.service.auth.client.GoogleOAuthClient;
import dev.devrunner.service.auth.client.dto.GoogleUserInfo;
import dev.devrunner.service.user.UserReader;
import dev.devrunner.service.user.UserWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 온보딩 서비스 구현체
 *
 * 회원가입, 사용자 존재 확인, 닉네임 중복 확인 등을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultUserOnboardingService implements UserOnboardingService {

    private final UserReader userReader;
    private final UserWriter userWriter;
    private final GoogleOAuthClient googleOAuthClient;

    @Override
    @Transactional
    public void signup(String accessToken, String nickname) {
        // 1. 닉네임 형식 검증
        validateNicknameFormat(nickname);

        // 2. Google API 호출
        GoogleUserInfo googleUserInfo = googleOAuthClient.getUserInfo(accessToken);

        // 3. Google ID로 기존 사용자 조회
        var existingUser = userReader.findByGoogleId(googleUserInfo.getGoogleId());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // 3-1. 탈퇴한 회원이면 재활성화
            if (user.getIsWithdrawn()) {
                User reactivated = user.reactivate();
                userWriter.upsert(reactivated);
                log.info("User reactivated: email={}, userId={}", user.getEmail(), user.getUserId());
                return;
            } else {
                // 3-2. 이미 활성 회원
                DuplicateUserException.throwByEmail(googleUserInfo.getEmail());
            }
        }

        // 4. 닉네임 중복 확인
        if (userReader.findByNickname(nickname).isPresent()) {
            DuplicateUserException.throwByNickname(nickname);
        }

        // 5. 신규 사용자 생성
        var newUser = User.newUser(
                googleUserInfo.getGoogleId(),
                googleUserInfo.getEmail(),
                nickname
        );

        userWriter.upsert(newUser);

        log.info("User signed up: email={}, nickname={}", newUser.getEmail(), nickname);
    }

    @Override
    @Transactional(readOnly = true)
    public UserCheckResult checkUser(String accessToken) {
        // 1. Google API 호출
        GoogleUserInfo googleUserInfo = googleOAuthClient.getUserInfo(accessToken);

        // 2. Google ID로 사용자 존재 여부 확인
        var user = userReader.findByGoogleId(googleUserInfo.getGoogleId());
        var exists = user.isPresent();
        if(exists && user.get().getIsWithdrawn()) {
            exists = false;
        }
        return UserCheckResult.of(exists, googleUserInfo.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public NicknameCheckResult checkNickname(String nickname) {
        // 1. 닉네임 형식 검증
        validateNicknameFormat(nickname);

        // 2. 중복 확인
        boolean exists = userReader.findByNickname(nickname).isPresent();

        if (exists) {
            return NicknameCheckResult.taken(nickname);
        } else {
            return NicknameCheckResult.available(nickname);
        }
    }

    /**
     * 닉네임 형식 검증
     *
     * 규칙: 4-15자의 영문자와 숫자 조합
     *
     * @param nickname 검증할 닉네임
     * @throws InvalidNicknameFormatException 형식이 올바르지 않을 경우
     */
    private void validateNicknameFormat(String nickname) {
        if (nickname == null || !nickname.matches("^[a-zA-Z0-9]{4,15}$")) {
            InvalidNicknameFormatException.throwInvalidFormat(nickname);
        }
    }
}
