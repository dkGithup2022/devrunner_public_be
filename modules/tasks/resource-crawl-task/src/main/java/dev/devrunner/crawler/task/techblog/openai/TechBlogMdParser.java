package dev.devrunner.crawler.task.techblog.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class TechBlogMdParser extends AbstractSingleGptRunner<String> {

    protected TechBlogMdParser(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, String.class);
    }

    @Override
    protected String getSystemPrompt() {
        return """
                # Task
                Convert the given **HTML/XML** into **clean Markdown** for post-processing. Keep structure, drop noise. Return **Markdown only**.
                
                # Rules
                - Map tags: headings→`#..######`, p→paragraph, br→line break, b/strong→**bold**, i/em→_italic_, code/pre→fenced code, ul/ol/li→lists, blockquote→`>`.
                - Links: `[text](url)`. Remove tracking params (`utm_*`, `fbclid`, `gclid`, `ref`).
                - Images: keep meaningful ones → `![alt](src)`. Skip tiny/tracker/decorative images.
                - Remove `<script>`, `<style>`, ads/nav/footer/share/iframes (keep plain video URL if obvious).
                - Tables: render as simple GFM table if data; otherwise flatten to lists/paragraphs.
                - Decode HTML entities. Normalize whitespace: max one blank line between blocks.
                - Keep original wording; **no fabrication** or summaries.
                - If already Markdown, just normalize (apply the same cleanup).
                
                # Output
                Only the cleaned **Markdown** (no extra text, no fences).
                """;
    }
}
