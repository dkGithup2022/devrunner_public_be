package dev.devrunner.crawler.task.job.contentCrawler.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class GoogleJobContentShortener extends AbstractSingleGptRunner<String> {
    protected GoogleJobContentShortener(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, String.class);
    }

    @Override
    protected String getSystemPrompt() {
        return """
                Role: Job posting body extractor.
                
                Goal:
                - From the given input text, keep **only the actual body content** of a single job posting.
                - Remove all irrelevant, repetitive, or boilerplate elements such as navigation menus, job lists, or footer disclaimers.
                - Output only the cleaned, readable job posting text (Markdown or plain text). No metadata, JSON, or explanations.
                
                Inclusion Rules (Allowlist):
                - Title (job name) and team/organization (if present)
                - Location and level (if mentioned)
                - Sections such as: About the job / Role summary / What you will do / Responsibilities / Requirements / Qualifications / Minimum / Preferred / Nice to have / Skills / Benefits / Location / Level
                - Keep only the core descriptive content of the position.
                - You may keep a single line indicating how to apply (e.g. “Apply on company site”) **only if it’s part of the main text**; otherwise, remove all apply buttons/links.
                
                Exclusion Rules (Blocklist):
                - Navigation, headers, breadcrumbs, menus (e.g. “Back to jobs search”, “Main menu”)
                - Job listing sections or search results (e.g. “Jobs search results”, “1–20 of …”, other position links)
                - Social sharing buttons or links (“Share”, “Copy link”, “Email a friend”)
                - Accessibility, disability accommodations, EEO/legal/privacy notices, agency disclaimers, “How we hire”, and other corporate policy boilerplate
                - Icon tokens and placeholder symbols (e.g. `_share_`, `_arrow_back_`, `_home_`, `_expand_more_`)
                - Repeated or generic footer text appearing across all job pages
                
                Formatting Rules:
                - Preserve a clean Markdown structure using only headings (`#`, `##`, `###`), bullet lists (`-`), and numbered lists (`1.`).
                - Remove duplicate blank lines and extra spaces.
                - Strip icon tokens, emojis, and decorative characters.
                - Deduplicate repeated phrases or boilerplate.
                - Keep links only if necessary for understanding (e.g. references to internal roles or documents). Remove all application URLs.
                - Keep the original language of the job post (do not translate).
                
                Detection Rules:
                - A valid job post typically contains at least two of the following: \s
                  “Responsibilities”, “Qualifications”, “About/Role description”, or a single distinct job title.
                - If the input primarily consists of a job **list page** or search results, remove everything and return an empty string.
                
                Output:
                - Return only the cleaned job posting body text (plain text or Markdown).
                - Do **not** include JSON, code blocks, or any explanatory text before or after the output.
                """;
    }
}
