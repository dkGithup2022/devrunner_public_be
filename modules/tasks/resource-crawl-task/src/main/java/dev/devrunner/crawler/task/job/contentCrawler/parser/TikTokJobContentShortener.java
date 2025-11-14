package dev.devrunner.crawler.task.job.contentCrawler.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;


@Component
public class TikTokJobContentShortener  extends AbstractSingleGptRunner<String> {

    protected TikTokJobContentShortener(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, String.class);
    }

    @Override
    protected String getSystemPrompt() {
        return """
                You are given a raw job posting document from the TikTok Careers site that may contain a mix of HTML, navigation menus, scripts, cookie banners, and other UI fragments. \s
                Your task is to **extract only the core job description content** and return it as **clean Markdown text**. \s
                Do **not** rewrite or summarize — perform **extraction only**.
                
                ### Output Rules (Minimal)
                1. **Output Markdown only:** \s
                   Remove all HTML tags, scripts/CSS, banners, navigation menus, sidebars, global footers, cookie consent popups, newsletter blocks, and non-content UI phrases such as \s
                   “#LifeAtTikTok”, “Apply”, “Search now”, “Accept all”, “Decline all”, etc. \s
                
                2. **Extract and preserve only the following sections** (if they exist, in the same order): \s
                
                3. **Preserve structure:** \s
                   - Keep all bullet and numbered lists exactly as in the original. \s
                   - Do not merge or summarize sentences. \s
                
                4. **Clean up links:** \s
                   - Keep only meaningful job-related or legal reference links. \s
                   - Remove tracking parameters (e.g. `utm_*`, `attr_source`, `refer=*`). \s
                
                5. **Remove redundancy/noise:** \s
                   - Delete repeated TikTok footer sections (“Programs”, “Resources”, “Legal”), repeated company intros, and cookie banners. \s
                   - Remove empty sections or decorative headers (like “Security” or “English日本語”). \s
                
                6. **Final output:** \s
                   Return one single Markdown document, with normalized blank lines and no leading or trailing whitespace.
                
                               """;
    }
}
