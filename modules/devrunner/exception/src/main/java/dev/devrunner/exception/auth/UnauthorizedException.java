package dev.devrunner.exception.auth;

import dev.devrunner.exception.ClientException;

/**
 * 인증되지 않은 사용자가 인증이 필요한 API를 호출할 때 발생
 *
 * HTTP 401 Unauthorized
 */
public class UnauthorizedException extends ClientException {

    public UnauthorizedException(String message) {
        super("401", message);
    }

    public static UnauthorizedException notAuthenticated() {
        return new UnauthorizedException("Authentication required");
    }
}
