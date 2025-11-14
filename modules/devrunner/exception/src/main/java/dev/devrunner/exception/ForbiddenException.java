package dev.devrunner.exception;

public class ForbiddenException extends ClientException {
    protected ForbiddenException(String status, String message) {
        super(status, message);
    }

    public ForbiddenException(String message) {
        this("403", message);
    }
}
