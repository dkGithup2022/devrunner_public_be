package dev.devrunner.exception.bookmark;

import dev.devrunner.exception.ClientException;

/**
 * 이미 북마크가 존재할 때 발생하는 예외
 * HTTP 409 Conflict
 */
public class DuplicateBookmarkException extends ClientException {

    public DuplicateBookmarkException( String message) {
        super("400", message);
    }
}
