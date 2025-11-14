package dev.devrunner.crawler.task.job.contentCrawler.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class GeneralJobContentShortener extends AbstractSingleGptRunner<String> {
    protected GeneralJobContentShortener(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, String.class);
    }

    @Override
    protected String getSystemPrompt() {
        return """
                Role: Job posting body extractor.

                Goal:
                Extract only the main job description in clean Markdown. Remove all navigation, footer, social, and legal boilerplate.

                Keep sections: Title, Team/Organization, Location, About, Role Summary, Responsibilities, Requirements, Skills, Qualifications, Benefits, Compensation.

                Remove: navigation menus, job lists, social links, "Apply now", privacy/cookie/EEO/legal text, and repeated footer or promotional content.

                Output clean Markdown only (headings/lists OK). No HTML, JSON, or commentary.
                If the input is not a single valid job posting, return an empty string.
                """;
    }
}
