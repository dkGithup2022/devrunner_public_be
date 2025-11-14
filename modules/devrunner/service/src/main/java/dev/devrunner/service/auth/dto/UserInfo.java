package dev.devrunner.service.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 사용자 정보 DTO
 *
 * Service 계층에서 사용하는 사용자 정보
 */
@Getter
@AllArgsConstructor
public class UserInfo {

    private Long userId;
    private String email;
    private String nickname;
    private String picture;
    private String googleId;

    public static UserInfo of(Long userId, String email,
                              String nickname, String picture, String googleId) {
        return new UserInfo(userId, email, nickname, picture, googleId);
    }
}
