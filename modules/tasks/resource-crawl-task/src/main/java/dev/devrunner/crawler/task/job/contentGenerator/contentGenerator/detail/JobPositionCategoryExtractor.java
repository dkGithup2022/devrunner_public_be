package dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail;


import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.model.job.PositionCategory;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
// TODO: Consider prompt A/B tests on real samples - consider add just software engineer
public class JobPositionCategoryExtractor
        extends AbstractSingleGptRunner<JobPositionCategoryExtractor.JobPositionCategoryResult> {

    protected JobPositionCategoryExtractor(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, JobPositionCategoryResult.class);
    }

    @Override
    protected String getSystemPrompt() {
        return PROMPT;
    }

    private static final String PROMPT = """
        Read the following job posting and classify the **Position Category** as exactly ONE of:

        - `BACKEND`: server-side APIs, databases, systems architecture
        - `FRONTEND`: web UI/UX, web application development
        - `FULLSTACK`: both frontend and backend responsibilities
        - `MOBILE`: Android, iOS, or cross-platform (e.g., Flutter)
        - `DATA`: data pipelines, analytics/BI, DWH engineering
        - `ML_AI`: machine learning models, LLM applications, AI modeling
        - `DEVOPS`: cloud infrastructure, CI/CD, observability/operations
        - `HARDWARE`: embedded systems, chipsets, electronics
        - `QA`: quality assurance, test engineering, QA ops
        - `NOT_CATEGORIZED`: use only if none of the above clearly applies

        Guidelines:
        - Even if multiple technologies are mentioned, choose the SINGLE most central role.
        - If responsibilities are vague, make the best determination based on the core duties.
        - Use `NOT_CATEGORIZED` only when a clear mapping is impossible.

        Return JSON in this exact shape:

        ```json
        {
          "positionCategory": "DEVOPS"
        }
        ```
        """;

    public record JobPositionCategoryResult(String positionCategory) {
        public PositionCategory getPositionCategoryEnum() {
            return PositionCategory.fromString(positionCategory);
        }

        public String getPositionCategory() {
            return positionCategory;
        }
    }
}