package dev.devrunner.crawler.task.techblog.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class TechBlogKoreanSummaryTranslator extends AbstractSingleGptRunner<String> {
    protected TechBlogKoreanSummaryTranslator(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, String.class);
    }

    @Override
    protected String getSystemPrompt() {
        return """
                Tech Keyword Phrases Translator (English → Korean)

                Translate the given English technical keyword phrases into natural Korean for search optimization.

                ### Translation Rules
                1. Translate ALL terms into Korean (including technical terms)
                   - JVM → 자바 가상 머신
                   - Spring Boot → 스프링 부트
                   - Redis → 레디스
                   - caching → 캐싱
                   - refactoring → 리팩토링
                2. Keep the same format: one phrase per line
                3. Do NOT add numbering, bullets, or extra explanations

                ### Example
                Input:
                JVM tuning for performance optimization
                Spring Boot refactoring for modularity
                Redis caching for reducing latency

                Output:
                자바 가상 머신 튜닝을 통한 성능 최적화
                스프링 부트 리팩토링으로 모듈화 개선
                레디스 캐싱을 통한 지연 시간 단축
                """;
    }
}
