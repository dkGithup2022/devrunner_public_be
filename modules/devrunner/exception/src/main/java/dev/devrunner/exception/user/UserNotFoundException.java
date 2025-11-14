package dev.devrunner.exception.user;

import dev.devrunner.exception.BadRequestException;

public class UserNotFoundException  extends BadRequestException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
