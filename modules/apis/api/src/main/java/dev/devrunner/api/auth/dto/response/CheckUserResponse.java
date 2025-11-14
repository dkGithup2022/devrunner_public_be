package dev.devrunner.api.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckUserResponse {

    private boolean exists;
    private String email;

    public static CheckUserResponse of(boolean exists, String email) {
        return new CheckUserResponse(exists, email);
    }
}
