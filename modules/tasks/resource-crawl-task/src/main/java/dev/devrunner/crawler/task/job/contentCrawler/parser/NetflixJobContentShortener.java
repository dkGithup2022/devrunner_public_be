package dev.devrunner.crawler.task.job.contentCrawler.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class NetflixJobContentShortener extends AbstractSingleGptRunner<String> {

    protected NetflixJobContentShortener(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, String.class);
    }

    @Override
    protected String getSystemPrompt() {
        return """
                    You are given a raw job posting document that may contain a mix of HTML, text, scripts, banners, and embedded JSON.  
                Your task is to **extract only the core job description content** and return it as **clean Markdown text**.  
                Do **not** rewrite or summarize — perform **extraction only**.
                
                ### Output Rules (Minimal)
                1. **Output Markdown only:** Remove all code blocks, HTML tags, scripts/CSS, banners, navigation bars, footers, cookie/recaptcha/theme/config JSON, and UI boilerplate text such as “Skip to main content”, “Apply now”, “Show more positions”, etc.  
                2. **Extract and preserve only the following sections** (if they exist, in the same order):  
                3. **Preserve list and text structure:** Keep all bullet and numbered lists as-is in Markdown. No paraphrasing or sentence rewriting.  
                4. **Clean up links:** Keep only meaningful external links and remove tracking parameters (`utm_*`, `microsite`, `domain`, etc.).  
                5. **Remove redundancy/noise:** Delete repeated phrases, promotional banners, and top/bottom UI blocks.  
                6. **Return a single Markdown document** with trimmed leading/trailing spaces and normalized blank lines.  
                """;
    }
}
