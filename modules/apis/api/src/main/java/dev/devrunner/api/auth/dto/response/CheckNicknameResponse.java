package dev.devrunner.api.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckNicknameResponse {

    private boolean available;
    private String message;

    public static CheckNicknameResponse available(String nickname) {
        return new CheckNicknameResponse(true, "This nickname is available.");
    }

    public static CheckNicknameResponse taken(String nickname) {
        return new CheckNicknameResponse(false, "This nickname is already taken.");
    }
}
