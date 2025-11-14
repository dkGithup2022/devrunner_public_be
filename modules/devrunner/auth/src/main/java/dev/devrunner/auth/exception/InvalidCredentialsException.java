package dev.devrunner.auth.exception;

/**
 * 로그인 인증 실패 예외
 *
 * 이메일 또는 비밀번호가 일치하지 않을 때 발생합니다.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
