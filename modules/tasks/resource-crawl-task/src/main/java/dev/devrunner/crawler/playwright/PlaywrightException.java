package dev.devrunner.crawler.playwright;

public class PlaywrightException extends RuntimeException {

    public PlaywrightException(String message) {
        super(message);
    }

    public PlaywrightException(String message, Throwable cause) {
        super(message, cause);
    }
}
