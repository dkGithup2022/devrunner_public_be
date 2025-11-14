package dev.devrunner.api.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {

    private Long userId;
    private String email;
   // private String name;
    private String nickname;
    private String picture;
    private String googleId;

    public static UserInfoResponse of(Long userId, String email,
                                      String nickname, String picture, String googleId) {
        return new UserInfoResponse(userId, email,  nickname, picture, googleId);
    }
}
