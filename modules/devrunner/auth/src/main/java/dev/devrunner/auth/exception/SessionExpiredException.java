package dev.devrunner.auth.exception;

/**
 * 세션 만료 예외
 *
 * 세션이 만료되었거나 존재하지 않을 때 발생합니다.
 */
public class SessionExpiredException extends RuntimeException {

    public SessionExpiredException(String message) {
        super(message);
    }

    public SessionExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
