package dev.devrunner.openai.base;

import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

/**
 * Factory for creating RestClient.Builder instances configured for OpenAI API compatibility.
 *
 * <p>This factory addresses a known issue with Spring AI and OpenAI API where chunked transfer
 * encoding responses can cause JSON parsing errors:
 * <pre>
 * org.springframework.http.converter.HttpMessageNotReadableException:
 *   JSON parse error: Unexpected end-of-input in VALUE_STRING
 * </pre>
 *
 * <p>Root Cause:
 * The OpenAI API may send responses using chunked transfer encoding without proper
 * Content-Length headers. When combined with certain HTTP client configurations, this can
 * result in incomplete JSON responses being parsed, leading to "Unexpected end-of-input" errors.
 *
 * <p>Solution:
 * By explicitly setting the "Accept-Encoding" header to "gzip, deflate", we ensure that
 * responses are properly compressed and decompressed, avoiding chunked encoding issues.
 *
 * <p>Reference:
 * - https://github.com/spring-projects/spring-ai/issues/372
 *
 * <p>Usage:
 * In your application's configuration class, register this as a bean:
 * <pre>{@code
 * @Configuration
 * public class AppConfig {
 *
 *     @Bean
 *     RestClient.Builder restClientBuilder() {
 *         return OpenAiRestClientBuilderFactory.createForOpenAi();
 *     }
 * }
 * }</pre>
 *
 * <p>Note:
 * This configuration is global for all RestClient instances in your application context.
 * Only apply this in modules that use Spring AI with OpenAI (e.g., batch-application,
 * resource-crawl-task tests). Do not apply globally if other modules need different
 * RestClient configurations.
 *
 * @since 1.0
 */
public class OpenAiRestClientBuilderFactory {

    private OpenAiRestClientBuilderFactory() {
        // Prevent instantiation
    }

    /**
     * Creates a RestClient.Builder configured for OpenAI API compatibility.
     *
     * <p>This builder includes the "Accept-Encoding: gzip, deflate" header to prevent
     * JSON parsing errors caused by chunked transfer encoding issues.
     *
     * @return a configured RestClient.Builder instance
     */
    public static RestClient.Builder createForOpenAi() {
        return RestClient.builder()
                .defaultHeaders(headers -> {
                    // Fix for: JSON parse error: Unexpected end-of-input in VALUE_STRING
                    // See: https://github.com/spring-projects/spring-ai/issues/372
                    headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate");
                });
    }
}
