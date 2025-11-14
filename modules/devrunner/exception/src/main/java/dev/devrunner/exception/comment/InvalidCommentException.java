package dev.devrunner.exception.comment;

import dev.devrunner.exception.BadRequestException;

/**
 * Invalid comment exception
 *
 * Thrown when comment validation fails (invalid content, userId, etc.)
 */
public class InvalidCommentException extends BadRequestException {
    public InvalidCommentException(String message) {
        super(message);
    }
}
