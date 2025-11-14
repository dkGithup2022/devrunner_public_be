package dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail;


import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobLocationExtractor
        extends AbstractSingleGptRunner<JobLocationExtractor.JobLocationResult> {

    protected JobLocationExtractor(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, JobLocationResult.class);
    }

    @Override
    protected String getSystemPrompt() {
        return PROMPT;
    }

    private static final String PROMPT = """
        From the following job posting, extract the **workplace locations** as an array of strings.

        Guidelines:
        - Look for mentions such as “office,” “workplace,” “location,” “primary site,” etc.
        - Return **geographic names only** (city/region/country/campus). If phrases like
          “remote,” “work from home,” “hybrid” appear together with a place, extract the place name(s) only.
        - If multiple locations are valid (multi-site or optional hubs), include all of them (deduplicated),
          ordered by prominence in the text. Keep it concise (e.g., "Seoul", "Pangyo", "Tokyo", "USA").
        - If no concrete place is given, return an **empty array**.

        Return **JSON only** in this exact shape:

        {
          "locations": ["Seoul", "Pangyo"]
        }
        """;

    public record JobLocationResult(List<String> locations) {}
}
