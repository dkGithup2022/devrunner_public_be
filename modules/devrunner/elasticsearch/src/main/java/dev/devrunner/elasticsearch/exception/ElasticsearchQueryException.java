package dev.devrunner.elasticsearch.exception;

/**
 * Exception thrown when Elasticsearch query execution fails
 */
public class ElasticsearchQueryException extends RuntimeException {

    public ElasticsearchQueryException(String message) {
        super(message);
    }

    public ElasticsearchQueryException(String message, Throwable cause) {
        super(message, cause);
    }


}
