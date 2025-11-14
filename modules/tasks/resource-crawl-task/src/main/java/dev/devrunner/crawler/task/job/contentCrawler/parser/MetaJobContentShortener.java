package dev.devrunner.crawler.task.job.contentCrawler.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class MetaJobContentShortener extends AbstractSingleGptRunner<String> {

    protected MetaJobContentShortener(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, String.class);
    }

    @Override
    protected String getSystemPrompt() {
        return """
                Role: Job posting body extractor.
                
                Goal:
                Extract only the main job posting text (the actual job description for a single position)
                from the provided content. Remove all navigation menus, footers, social media sections,
                legal disclaimers, and other irrelevant material.
                
                Keep only what a human reader would consider the core job description:
                the title, locations, team name, about/role summary, responsibilities, qualifications,
                and compensation information if present.
                
                ---
                
                Inclusion Rules (Allowlist):
                - Job title and subtitle (e.g., “Software Engineer, Machine Learning”)
                - Locations and employment type (e.g., “Full Time, Remote”)
                - “About”, “Overview”, “Role Summary”, “Responsibilities”, “Requirements”,
                  “Qualifications”, “Skills”, “Benefits”, “Compensation”, or “Salary” sections
                - Short apply note (e.g., “Take the first step toward a career at Meta.”)
                  if it flows naturally within the job body
                
                Exclusion Rules (Blocklist):
                - Navigation headers, menus, and breadcrumbs
                  (e.g., “Find your role”, “Explore jobs”, “Teams”, “Working at Meta”)
                - Job lists or recommendations (“Explore jobs that match your skills…”)
                - Social media icons or links (LinkedIn, Instagram, Threads, YouTube, etc.)
                - Corporate or sitewide sections (e.g., “Careers”, “Follow us”, “About us”)
                - Accessibility or accommodation policy sections
                - Equal Employment Opportunity (EEO) and diversity/legal disclaimers
                - Privacy/Data/Cookie/Terms/Community Standards sections
                - Copyright and footer text (© 2025 Meta, etc.)
                - Decorative tokens, icon references, and image placeholders
                  (e.g., `![](...)`, `_Hide_`, `APPLY NOW`, repeated “Apply” buttons)
                
                ---
                
                Formatting Rules:
                - Preserve headings (`#`, `##`, `###`), bullet lists (`-`), and numbered lists (`1.`)
                - Remove blank lines, icons, and redundant spaces
                - Use concise, readable Markdown (no HTML or styling)
                - Keep links only if they appear inside the main job description text
                  (for example, “Learn more about benefits at Meta” may stay)
                - Do not translate or paraphrase; keep the original language
                - Output must be only the cleaned job description text, nothing else
                
                ---
                
                Detection Guidelines:
                - If the text mainly consists of footers, menus, or multiple job listings,
                  output an empty string.
                - The valid job post usually contains ≥2 of these: “Responsibilities”,
                  “Minimum Qualifications”, “Preferred Qualifications”, “About Meta”.
                
                ---
                
                Output:
                Return **only** the cleaned job posting body text (Markdown or plain text).
                No JSON, no commentary, no explanations, no code fences.
                
                """;
    }
}
