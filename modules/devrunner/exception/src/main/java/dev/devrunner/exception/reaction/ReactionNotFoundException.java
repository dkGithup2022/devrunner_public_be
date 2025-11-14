package dev.devrunner.exception.reaction;

import dev.devrunner.exception.BadRequestException;

public class ReactionNotFoundException extends BadRequestException {
    public ReactionNotFoundException(String message) {
        super(message);
    }
}
