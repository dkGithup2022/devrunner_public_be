package dev.devrunner.crawler.task.job.contentCrawler.validator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

/**
 * 채용 공고 마크다운 유효성 검증기
 * <p>
 * GPT를 사용하여 크롤링된 콘텐츠가 실제 채용 공고인지 검증
 */
@Component
public class HiringMarkDownValidator extends AbstractSingleGptRunner<HiringMarkDownValidator.IsValidHiringMd> {

    protected HiringMarkDownValidator(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, IsValidHiringMd.class);
    }

    @Override
    protected String getSystemPrompt() {
        return PROMPT;
    }

    private static final String PROMPT = """
            You are a hiring content validator.

            Determine if the given markdown is a VALID job posting based on these criteria:

            1. **Purpose**: Must be related to job recruitment/hiring
               - Contains: job title, responsibilities, qualifications, or application info
               - Excludes: company news, blog posts, general announcements

            2. **Content Quality**: Must have sufficient detail
               - At least 3 sections (e.g., role description, requirements, benefits)
               - Not just images, links, or placeholder text
               - Actual job-related information (not "content unavailable", "page not found", etc.)

            3. **Language**: Any language is acceptable (English, Korean, etc.)

            Return JSON format:
            ```json
            {
                "valid": true,
                "reason": "valid job posting with clear role description and requirements"
            }
            ```

            If invalid, provide specific reason:
            ```json
            {
                "valid": false,
                "reason": "not hiring related - appears to be a blog post"
            }
            ```
            """;

    /**
     * 채용 공고 유효성 검증 결과
     */
    public record IsValidHiringMd(
            @JsonProperty("valid")
            boolean valid,

            @JsonProperty("reason")
            String reason
    ) {
    }
}
