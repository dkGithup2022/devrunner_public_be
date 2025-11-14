package dev.devrunner.exception.job;

import dev.devrunner.exception.BadRequestException;

public class JobNotFoundException extends BadRequestException {
    public JobNotFoundException(String message) {
        super(message);
    }
}
