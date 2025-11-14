package dev.devrunner.exception;

public class BadRequestException extends ClientException {
    protected BadRequestException(String status, String message) {
        super(status, message);
    }

    public BadRequestException(String message) {
        this("400", message);
    }
}

