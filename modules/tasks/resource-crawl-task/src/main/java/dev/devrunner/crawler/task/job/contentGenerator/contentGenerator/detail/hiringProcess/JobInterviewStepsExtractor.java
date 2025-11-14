package dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail.hiringProcess;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class JobInterviewStepsExtractor
        extends AbstractSingleGptRunner<JobInterviewStepsExtractor.JobInterviewStepsResult> {

    protected JobInterviewStepsExtractor(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, JobInterviewStepsResult.class);
    }

    @Override
    protected String getSystemPrompt() {
        return PROMPT;
    }

    public static final String PROMPT = """
        Read the following job posting and extract **objective steps** of the selection process.

        Return these fields:
        - `hasCodingTest`: true if any coding test is mentioned (online/offline), false if explicitly absent, null if not stated.
        - `hasLiveCoding`: true if "live coding/on-site coding/pair coding during interview" is explicitly mentioned, false if explicitly absent, null if not stated.
        - `hasAssignment`: true if a take-home/portfolio/technical assignment round is explicitly mentioned, false if explicitly absent, null if not stated.
        - `interviewCount`: number of interviews **excluding phone/recruiter screens** (e.g., technical, culture fit, executive). Integer or null if unclear.
        - `interviewDays`: number of **distinct calendar days** required if explicitly stated (e.g., "one-day onsite"). Integer or null if not stated.

        Rules:
        - Do **not** guess. Use null unless there is clear evidence in the text.
        - Count multi-stage interviews separately when explicitly listed (e.g., "2 technical + 1 culture fit" → 3).
        - Phone/recruiter screens are **not** included in `interviewCount`.

        Return **JSON only** in the exact shape:

        ```json
        {
          "hasCodingTest": true,
          "hasLiveCoding": false,
          "hasAssignment": false,
          "interviewCount": 3,
          "interviewDays": 1
        }
        ```
        """;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JobInterviewStepsResult(
            Boolean hasCodingTest,
            Boolean hasLiveCoding,
            Boolean hasAssignment,
            Integer interviewCount,
            Integer interviewDays
    ) {}
}
