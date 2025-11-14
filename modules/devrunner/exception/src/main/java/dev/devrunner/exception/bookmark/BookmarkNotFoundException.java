package dev.devrunner.exception.bookmark;

import dev.devrunner.exception.ClientException;

/**
 * 북마크를 찾을 수 없을 때 발생하는 예외
 * HTTP 404 Not Found
 */
public class BookmarkNotFoundException extends ClientException {

    public BookmarkNotFoundException(String message) {
        super("404", message);
    }
}
