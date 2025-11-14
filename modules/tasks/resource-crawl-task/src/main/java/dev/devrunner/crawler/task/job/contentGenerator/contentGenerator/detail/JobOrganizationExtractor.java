package dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class JobOrganizationExtractor
        extends AbstractSingleGptRunner<JobOrganizationExtractor.JobOrganizationResult> {

    protected JobOrganizationExtractor(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, JobOrganizationResult.class);
    }

    @Override
    protected String getSystemPrompt() {
        return PROMPT;
    }

    private static final String PROMPT = """
        From the following job posting, extract the **team / organization / subsidiary name** (if any).

        Guidance:
        - Look for phrases like “part of the XX team,” “belong to XX subsidiary,” “XX CIC,” etc.
        - If a team or subsidiary is explicitly named, return that exact string (e.g., "Search CIC", "Google Ads Team", "Netflix data pipeline ", "Data Lab").
        - If multiple teams are listed, choose the **primary** one most central to the role.
        - If nothing explicit is provided, respond with `null`.

        Return JSON in the following format:

        ```json
        {
          "organization": "Search CIC"
        }
        ```
        If unknown, use:
        ```json
        {
          "organization": null
        }
        ```
        """;

    public record JobOrganizationResult(String organization) {}
}