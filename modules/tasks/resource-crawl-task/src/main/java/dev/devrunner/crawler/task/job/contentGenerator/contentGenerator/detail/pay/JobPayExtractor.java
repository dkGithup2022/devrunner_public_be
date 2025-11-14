package dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail.pay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;


@Component
public class JobPayExtractor extends AbstractSingleGptRunner<JobPayExtractor.JobPayResult> {

    protected JobPayExtractor(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, JobPayResult.class);
    }

    @Override
    protected String getSystemPrompt() {
        return PROMPT;
    }

    public static final String PROMPT = """
        Extract the **compensation or pay section** from the following job posting.

        Rules:
        1. If a compensation section exists, **preserve the original wording** (no paraphrasing).
        2. If it appears in multiple places, combine them with a line break.
        3. Includes mentions of salary, pay range, stock options, bonuses, and extra paid leave.
        4. If no such section exists, return null.

        Output JSON:
        {
          "section": "The US base salary range for this full-time position is $141,000–$202,000 + bonus + equity + benefits."
        }

        Example (no section):
        {
          "section": null
        }
        """;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JobPayResult(String section) {}
}