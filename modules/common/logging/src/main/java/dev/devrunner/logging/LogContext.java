package dev.devrunner.logging;

import org.slf4j.MDC;

import java.time.Instant;
import java.util.UUID;

/**
 * Utility class for managing logging context with MDC
 */
public final class LogContext {

    private LogContext() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Set MDC value using enum key
     */
    public static void put(LoggingKey key, String value) {
        if (value != null) {
            MDC.put(key.getKey(), value);
        }
    }

    /**
     * Set MDC value with Object (converted to String)
     */
    public static void put(LoggingKey key, Object value) {
        if (value != null) {
            MDC.put(key.getKey(), value.toString());
        }
    }

    /**
     * Get MDC value
     */
    public static String get(LoggingKey key) {
        return MDC.get(key.getKey());
    }

    /**
     * Remove specific MDC key
     */
    public static void remove(LoggingKey key) {
        MDC.remove(key.getKey());
    }

    /**
     * Clear all MDC values
     */
    public static void clear() {
        MDC.clear();
    }

    /**
     * Generate and set trace ID
     */
    public static String generateTraceId() {
        String traceId = UUID.randomUUID().toString();
        put(LoggingKey.TRACE_ID, traceId);
        return traceId;
    }

    /**
     * Set API context
     */
    public static void setApiContext(String endpoint, String method) {
        generateTraceId();
        put(LoggingKey.ENDPOINT, endpoint);
        put(LoggingKey.HTTP_METHOD, method);
    }

    /**
     * Set batch context
     */
    public static void setBatchContext(String jobName) {
        put(LoggingKey.JOB_NAME, jobName);
        put(LoggingKey.STARTED_AT, Instant.now());
    }

    /**
     * Clear API context
     */
    public static void clearApiContext() {
        remove(LoggingKey.TRACE_ID);
        remove(LoggingKey.ENDPOINT);
        remove(LoggingKey.HTTP_METHOD);
        remove(LoggingKey.USER_ID);
    }

    /**
     * Clear batch context
     */
    public static void clearBatchContext() {
        remove(LoggingKey.JOB_NAME);
        remove(LoggingKey.STARTED_AT);
    }
}
