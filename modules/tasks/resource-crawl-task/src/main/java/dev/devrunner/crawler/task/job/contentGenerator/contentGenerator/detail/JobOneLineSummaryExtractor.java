package dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail;


import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class JobOneLineSummaryExtractor
        extends AbstractSingleGptRunner<JobOneLineSummaryExtractor.JobOneLineSummaryResult> {

    protected JobOneLineSummaryExtractor(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, JobOneLineSummaryResult.class);
    }

    @Override
    protected String getSystemPrompt() {
        return PROMPT;
    }

    private static final String PROMPT = """
            Read the job posting below and produce a **single-line** summary of the core work or project
            (keep it concise, ideally within 8–15 words).

            Rules:
            - Do NOT include company, organization, or team names.
            - If a concrete project name exists, returning just that project name is acceptable.
              - e.g., "고객 지원 자동화 시스템"
            - If responsibilities are clear, write one sentence capturing the most important duty.
              - e.g., "고객 문의 데이터를 활용한 셀프 서비스 자동화 시스템 구축"
            - Avoid specific service brand names; use generic wording (e.g., avoid "within FooApp").
            - Avoid overly generic phrases; the line should reveal the real work.
              - ❌ "서버 개발", "백엔드 개발"
              - ✅ "주문 후 고객 경험을 위한 CS 시스템 개발"
            - **Output in Korean language**.

            Output **JSON only** in the following shape:

            ```json
            {
              "oneLineSummary": "대규모 트래픽 개인화 서비스를 위한 백엔드 설계 및 운영"
            }```
            """;

    public record JobOneLineSummaryResult(String oneLineSummary) {
    }
}
