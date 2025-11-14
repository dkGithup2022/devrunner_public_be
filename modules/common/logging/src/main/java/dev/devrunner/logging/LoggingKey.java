package dev.devrunner.logging;

/**
 * Logging context field keys for MDC
 */
public enum LoggingKey {
    // Common fields
    TRACE_ID("traceId"),

    // API specific fields
    ENDPOINT("endpoint"),
    HTTP_METHOD("method"),
    USER_ID("userId"),

    // Batch specific fields
    JOB_NAME("jobName"),
    STARTED_AT("startedAt");

    private final String key;

    LoggingKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return key;
    }
}
