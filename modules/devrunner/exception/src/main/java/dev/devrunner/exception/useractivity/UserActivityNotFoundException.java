package dev.devrunner.exception.useractivity;

import dev.devrunner.exception.ClientException;

public class UserActivityNotFoundException extends ClientException {
    public UserActivityNotFoundException(String message) {
        super(message);
    }
}
