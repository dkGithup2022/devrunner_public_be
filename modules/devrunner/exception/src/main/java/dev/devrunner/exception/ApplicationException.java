package dev.devrunner.exception;

public abstract class ApplicationException extends RuntimeException {
    private final String status;
    private final String message;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    protected ApplicationException(String status, String message) {
        this.status = status;
        this.message = message;
    }

}

