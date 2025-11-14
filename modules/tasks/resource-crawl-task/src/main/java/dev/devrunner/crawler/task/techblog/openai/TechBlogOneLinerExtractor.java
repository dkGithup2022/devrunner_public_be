package dev.devrunner.crawler.task.techblog.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import dev.devrunner.openai.base.SingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class TechBlogOneLinerExtractor extends AbstractSingleGptRunner<TechBlogOneLinerExtractor.OneLineResult> {
    protected TechBlogOneLinerExtractor(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, OneLineResult.class);
    }

    @Override
    protected String getSystemPrompt() {
        return"""
              Tech Blog Summary

              Summarize the core technical topic in **one line** (20-40 characters) **in Korean**.

              Rules:
              - Include numerical metrics when available (%, speed, latency, etc.)
              - Exclude company/team/brand names
              - Focus on concrete technical outcomes, not generic descriptions

              Examples:
              - ✅ "Elasticsearch 샤딩 최적화로 인프라 비용 50% 절감"
              - ✅ "DDA 알고리즘으로 LLM 추론 속도 10-30% 향상"
              - ❌ "AI 모델 최적화 공유"

              Return JSON:
              ```json
              {
                "oneLineSummary": "32노드 분산처리로 1M토큰 77초 달성"
              }
              ```
              """;
    }

    public record OneLineResult(
            String oneLineSummary
    ) {
    }
}
