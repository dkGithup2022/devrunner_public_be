package dev.devrunner.service.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 닉네임 사용 가능 여부 확인 결과
 *
 * Service 계층에서 사용하는 닉네임 체크 결과
 */
@Getter
@AllArgsConstructor
public class NicknameCheckResult {

    private boolean available;
    private String message;

    public static NicknameCheckResult available(String nickname) {
        return new NicknameCheckResult(true, "This nickname is available.");
    }

    public static NicknameCheckResult taken(String nickname) {
        return new NicknameCheckResult(false, "This nickname is already taken.");
    }
}
