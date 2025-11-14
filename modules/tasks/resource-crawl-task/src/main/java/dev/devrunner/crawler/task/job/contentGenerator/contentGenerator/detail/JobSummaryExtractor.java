package dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class JobSummaryExtractor  extends AbstractSingleGptRunner<String>{
    protected JobSummaryExtractor(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, String.class);
    }
    @Override
    protected String getSystemPrompt() {
        return """
                Job Posting Keyword & Purpose Extractor
                
                Analyze the following job posting and extract 10–15 concise keyword phrases
                that summarize the main technical topics and their purposes or business contexts.
                
                Each phrase should naturally express what the technology or method is used for,
                and — when possible — hint at the related business domain (e.g., payment, logistics, commerce).
                
                ##Guidelines
               
                - Focus on core technical concepts, architectures, or workflows mentioned in the job description.
                - Include purpose or business context (e.g., improve reliability, reduce latency, automate logistics, optimize payment flow).
                - Avoid company names, numbers, or generic terms.
                - Use English only and keep phrases short (3–7 words).
                - Aim for 10–15 phrases total.
                - Use exact terminology from the posting when possible.
                
                ## EXAMPLE
                ```
                Backend Engineer tasks using Java, Spring \s
                JVM tuning for performance optimization \s
                Kafka streaming for real-time data processing \s
                Elasticsearch indexing for efficient retrieval \s
                Payment pipeline design for fraud prevention \s
                Logistics routing for delivery optimization \s
                Recommendation modeling for commerce growth \s
                Ad bidding system for campaign performance
                ```
                """;
    }
}

