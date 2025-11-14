package dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail.hiringProcess;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class JobHiringProcessExtractor
        extends AbstractSingleGptRunner<JobHiringProcessExtractor.JobHiringProcessResult> {

    protected JobHiringProcessExtractor(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, JobHiringProcessResult.class);
    }

    @Override
    protected String getSystemPrompt() {
        return PROMPT;
    }

    public static final String PROMPT = """
        Extract only the **hiring/recruitment process** from the following job posting and return JSON in the format below.

        Rules:
        - If a hiring process exists, **preserve the original wording** (no paraphrasing).
        - Extract **only** the hiring process section/content (do not include unrelated text).
        - If there is no hiring process at all, return `"hiringProcess": null`.

        Output format:
        ```json
        {
          "hiringProcess": "Application → Screening Task → Technical Interview → Culture Fit Interview → Reference Check → Offer → Onboarding"
        }
        ```

        Example 1:
        ```json
        {
          "hiringProcess": "How you join:\n1) Resume Review → 2) Assignment → 3) Interview(s) → 4) Reference Check → 5) Offer → 6) Start"
        }
        ```

        Example 2:
        ```json
        {
          "hiringProcess": null
        }
        ```
        """;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JobHiringProcessResult(String hiringProcess) {}
}