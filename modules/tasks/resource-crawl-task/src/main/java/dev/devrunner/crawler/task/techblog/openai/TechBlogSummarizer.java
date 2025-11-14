package dev.devrunner.crawler.task.techblog.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class TechBlogSummarizer extends AbstractSingleGptRunner<String> {
    protected TechBlogSummarizer(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, String.class);
    }

    @Override
    protected String getSystemPrompt() {
        return """
                Tech Blog Keyword + Purpose Extractor
                
                Analyze the following tech blog article and extract **10–15 concise keyword phrases** \s
                that represent the main **technical concepts and their purposes or actions**.
                
                Each phrase should clearly express **what the topic is** and **why or how it is used**.
                
                ### Output format
                Plain list (one phrase per line, no numbering, no bullets, no explanations)
                
                Each line: \s
                → “<technical concept or method> for <purpose/action>”
                
                ### Guidelines
                1. Focus on major technologies, architectures, algorithms, or methodologies mentioned. \s
                2. Include an action or purpose (e.g., improve, optimize, accelerate, enable, introduce, measure). \s
                3. Exclude numbers, company names
                4. Exclude generic/common terms: data, software, performance, system, application, service, solution, platform, tool, framework (unless part of a specific name)
                5. Use specific technical terms instead of generic ones (e.g., "Redis cache" not "data cache", "React hooks" not "software hooks")
                6. Use English only and keep phrases short (3–6 words). \s
                7. Aim for 10–15 phrases total. \s
                8. Extract keywords using exact terminology from the article text whenever possible
                
                ### Example
                JVM garbage collection tuning for memory efficiency\s
                Spring Boot modularization with multi-module structure\s
                Redis cache implementation for query acceleration\s
                Next.js SSR for search engine visibility\s
                React hooks refactoring for code reusability\s
                Kafka event streaming for asynchronous messaging
                Elasticsearch inverted index for full-text search
                Kubernetes pod autoscaling for traffic handling
                GraphQL resolver optimization for N+1 prevention
                """;
    }
}
