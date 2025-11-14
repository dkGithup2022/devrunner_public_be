package dev.devrunner.exception.user;

import dev.devrunner.exception.ClientException;

/**
 * 닉네임 형식 오류
 *
 * HTTP 400 Bad Request
 */
public class InvalidNicknameFormatException extends ClientException {

    public InvalidNicknameFormatException(String message) {
        super("400", message);
    }

    public static void throwInvalidFormat(String nickname) {
        throw new InvalidNicknameFormatException(
            "Nickname must be 4-15 characters and contain only English letters and numbers. Current: '" + nickname + "'"
        );
    }
}
