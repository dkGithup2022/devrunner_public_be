package dev.devrunner.application.batch.config;


import dev.devrunner.openai.base.OpenAiRestClientBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


/**
 * RestClient configuration for batch application.
 * Configures RestClient.Builder for OpenAI API compatibility.
 */
@Configuration
public class RestClientConfig {

    /**
     * Configures RestClient.Builder for OpenAI API compatibility.
     * Fixes "JSON parse error: Unexpected end-of-input in VALUE_STRING" issue.
     *
     * @see OpenAiRestClientBuilderFactory for detailed explanation
     */
    @Bean
    RestClient.Builder restClientBuilder() {
        return OpenAiRestClientBuilderFactory.createForOpenAi();
    }
}
