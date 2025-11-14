package dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail.explanation;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class JobPositionIntroductionExtractor
        extends AbstractSingleGptRunner<JobPositionIntroductionExtractor.JobPositionIntroductionResult> {

    protected JobPositionIntroductionExtractor(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, JobPositionIntroductionResult.class);
    }

    @Override
    protected String getSystemPrompt() {
        return PROMPT;
    }

    private static final String PROMPT = """
        From the following job posting, write a concise **two-sentence** summary that covers:
        1) which team/organization this role belongs to, and
        2) the primary mission/problems the role tackles.

        Rules:
        - Exclude HR greetings, benefits/perks, hiring process, and generic fluff.
        - Use natural, compact Korean language.
        - Sentences must be complete and reflect specifics found in the posting.
        - Do not invent facts; summarize only what's stated.
        - If writing multiple sentences, separate them with actual line breaks for readability.

        Return **JSON only** in this shape:

        ```json
        {
          "introduction": "커머스 플랫폼 팀 소속으로, 대규모 트래픽 환경에서 안정적인 주문 시스템을 설계하고 운영합니다.
고객 경험 개선을 위한 새로운 기능을 지속적으로 개발합니다."
        }
        ```
        """;

    public record JobPositionIntroductionResult(String introduction) {}
}