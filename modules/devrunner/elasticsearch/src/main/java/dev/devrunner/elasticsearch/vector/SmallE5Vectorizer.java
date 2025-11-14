package dev.devrunner.elasticsearch.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmallE5Vectorizer implements E5Vectorizer {

    private final RestTemplate vectorApiRestTemplate;

    @Value("${api.sentence_transformer.url}")
    private String vectorApiUrl;

    /**
     * Vectorize sentence using external Python API
     *
     * Retry configuration:
     * - Max attempts: 4
     * - Initial delay: 500ms
     * - Max delay: 1000ms
     * - Multiplier: 2
     */
    @Override
    @Retryable(
            maxAttempts = 4,
            backoff = @Backoff(delay = 500, multiplier = 2, maxDelay = 1000)
    )
    public List<Float> vectorize(String content) {
        if (content == null || content.isBlank()) {
            log.warn("Empty content provided for vectorization");
            return List.of();
        }

        log.info("Call vectorize python api & Try vectorize sentence");

        try {
            // Prepare request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            VectorizePayload payload = new VectorizePayload(content);
            HttpEntity<VectorizePayload> request = new HttpEntity<>(payload, headers);

            // Call API
            String url = vectorApiUrl + "/vectorize";
            float[] response = vectorApiRestTemplate.postForObject(url, request, float[].class);

            log.info("Sentence vectorized successfully");

            return castToList(response);

        } catch (Exception e) {
            log.error("Failed to vectorize sentence: {}", e.getMessage(), e);
            throw new VectorizeException("Failed to vectorize sentence", e);
        }
    }

    private List<Float> castToList(float[] floats) {
        if (floats == null) {
            return List.of();
        }

        List<Float> result = new ArrayList<>(floats.length);
        for (float f : floats) {
            result.add(f);
        }
        return result;
    }
}
