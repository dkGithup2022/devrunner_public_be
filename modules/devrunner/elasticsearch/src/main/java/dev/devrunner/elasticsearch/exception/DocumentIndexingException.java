package dev.devrunner.elasticsearch.exception;

/**
 * Exception thrown when document indexing fails
 */
public class DocumentIndexingException extends RuntimeException {

    public DocumentIndexingException(String message) {
        super(message);
    }

    public DocumentIndexingException(String message, Throwable cause) {
        super(message, cause);
    }


}
