package dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail.explanation;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobResponsibilitiesExtractor
        extends AbstractSingleGptRunner<JobResponsibilitiesExtractor.JobResponsibilitiesResult> {

    protected JobResponsibilitiesExtractor(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, JobResponsibilitiesResult.class);
    }

    @Override
    protected String getSystemPrompt() {
        return PROMPT;
    }

    private static final String PROMPT = """
        From the following job posting, extract the concrete **responsibilities** this role will perform.
        Return **between 2 and 6 items**.

        Guidelines:
        - Use short, action-oriented phrases that **start with a verb** (e.g., "서비스 운영 자동화", "Spring API 개발").
        - Be specific; summarize tasks explicitly mentioned in the posting (avoid vague catch-alls).
        - Exclude culture notes, benefits, hiring process, and team introductions.
        - De-duplicate similar items and keep each bullet concise (no full sentences).
        - Do not invent information; extract only what's stated.
        - **Output in Korean language**.

        Return **JSON only** in this exact shape:

        {
          "responsibilities": [
            "iOS 앱 기능 개발 및 유지보수",
            "레거시 코드 리팩토링 및 아키텍처 개선",
            "iOS/Android 간 일관된 UX 구현"
          ]
        }
        """;

    public record JobResponsibilitiesResult(List<String> responsibilities) {}
}