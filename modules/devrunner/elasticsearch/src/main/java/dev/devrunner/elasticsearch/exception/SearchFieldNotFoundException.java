package dev.devrunner.elasticsearch.exception;

/**
 * Exception thrown when a search field has no registered query builder
 */
public class SearchFieldNotFoundException extends RuntimeException {

    public SearchFieldNotFoundException(String fieldName) {
        super("No query builder found for field: " + fieldName);
    }

    public SearchFieldNotFoundException(String fieldName, Throwable cause) {
        super("No query builder found for field: " + fieldName, cause);
    }
}
