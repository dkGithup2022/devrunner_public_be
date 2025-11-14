package dev.devrunner.elasticsearch.vector;

/**
 * Request payload for vectorization API
 */
public record VectorizePayload(
        String param
) {
}
