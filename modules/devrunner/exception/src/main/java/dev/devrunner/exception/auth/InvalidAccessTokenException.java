package dev.devrunner.exception.auth;

import dev.devrunner.exception.ClientException;

/**
 * Google Access Token이 유효하지 않을 때 발생하는 예외
 *
 * HTTP 401 Unauthorized
 */
public class InvalidAccessTokenException extends ClientException {

    public InvalidAccessTokenException(String message) {
        super("401", message);
    }

    public static InvalidAccessTokenException create() {
        return new InvalidAccessTokenException("Invalid or expired Google access token");
    }

    public static InvalidAccessTokenException withCause(Throwable cause) {
        var exception = create();
        exception.initCause(cause);
        return exception;
    }
}
