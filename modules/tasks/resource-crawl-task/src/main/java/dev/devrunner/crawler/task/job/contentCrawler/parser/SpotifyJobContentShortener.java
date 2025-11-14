package dev.devrunner.crawler.task.job.contentCrawler.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class SpotifyJobContentShortener extends AbstractSingleGptRunner<String> {

    protected SpotifyJobContentShortener(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, String.class);
    }

    @Override
    protected String getSystemPrompt() {

        return
                """
                        You are given a raw job posting from the Spotify Careers site (https://www.lifeatspotify.com), which may include HTML, navigation menus, cookie notices, forms, images, or duplicated sections.  
                        Extract **only the main job description content** and return it as **pure Markdown text**.  
                        Do **not** summarize, paraphrase, or rewrite — perform extraction only.
                        
                        ### Output Rules
                        - Output **Markdown only**. Remove all HTML tags, scripts, forms, images, cookie banners, nav/footers, application fields, and duplicated elements (e.g., “Apply now”, “Similar jobs”, “How we hire”, “© 2025 Spotify AB.”).  
                        - Keep only these sections, if they exist (in order):  
                          **Title**, **Meta info** (Job Category / Level / Location / Type), **Overview / Description**, **What You’ll Do**, **Who You Are**, **Where You’ll Be**, **About Spotify / Inclusion & Diversity**, **Benefits**, **Additional Notes**.  
                        - Preserve bullet and numbered lists as Markdown lists; retain natural paragraph breaks.  
                        - Keep only meaningful links (official company or legal references); remove all tracking or decorative URLs (`utm_*`, `dl_branch`, `nd=*`, etc.).  
                        - Remove “Similar Jobs”, “Quick Clicks”, “Communities”, “Stay Updated”, and “Application Form” content.  
                        - Return a single, clean Markdown document with normalized spacing and no trailing whitespace.
                        
                        """;
    }
}
