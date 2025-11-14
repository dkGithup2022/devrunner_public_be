package dev.devrunner.elasticsearch.vector;

/**
 * Exception thrown when vectorization fails
 */
public class VectorizeException extends RuntimeException {

    public VectorizeException(String message) {
        super(message);
    }

    public VectorizeException(String message, Throwable cause) {
        super(message, cause);
    }
}
