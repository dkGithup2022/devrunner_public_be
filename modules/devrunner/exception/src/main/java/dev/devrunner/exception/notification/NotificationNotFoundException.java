package dev.devrunner.exception.notification;

import dev.devrunner.exception.ClientException;

public class NotificationNotFoundException extends ClientException {
    public NotificationNotFoundException(String message) {
        super(message);
    }
}
