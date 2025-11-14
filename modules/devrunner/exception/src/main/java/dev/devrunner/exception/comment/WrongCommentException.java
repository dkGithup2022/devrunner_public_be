package dev.devrunner.exception.comment;

import dev.devrunner.exception.BadRequestException;

public class WrongCommentException extends BadRequestException {
    public WrongCommentException(String message) {
        super(message);
    }
}
