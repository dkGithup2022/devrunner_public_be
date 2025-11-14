package dev.devrunner.exception.comment;

import dev.devrunner.exception.BadRequestException;

public class CommentNotFoundException extends BadRequestException {

    public CommentNotFoundException(String message) {
        super(message);
    }
}
