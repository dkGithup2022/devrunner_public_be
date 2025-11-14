package dev.devrunner.exception.user;

import dev.devrunner.exception.ClientException;

/**
 * 사용자 중복 (이메일 또는 닉네임)
 *
 * HTTP 409 Conflict
 */
public class DuplicateUserException extends ClientException {

    public DuplicateUserException(String message) {
        super("409", message);
    }

    public static void throwByEmail(String email) {
        throw new DuplicateUserException("User already exists with email: " + email);
    }

    public static void throwByNickname(String nickname) {
        throw new DuplicateUserException("Nickname already taken: " + nickname);
    }
}
