package dev.devrunner.exception.reaction;

import dev.devrunner.exception.BadRequestException;
import dev.devrunner.exception.ClientException;


public class ReactionConflictException extends ClientException {
    public ReactionConflictException(String message) {
        super("409", message);
    }
}
