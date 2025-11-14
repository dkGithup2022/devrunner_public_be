package dev.devrunner.service.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 사용자 존재 여부 확인 결과
 *
 * Service 계층에서 사용하는 사용자 체크 결과
 */
@Getter
@AllArgsConstructor
public class UserCheckResult {

    private boolean exists;
    private String email;

    public static UserCheckResult of(boolean exists, String email) {
        return new UserCheckResult(exists, email);
    }
}
