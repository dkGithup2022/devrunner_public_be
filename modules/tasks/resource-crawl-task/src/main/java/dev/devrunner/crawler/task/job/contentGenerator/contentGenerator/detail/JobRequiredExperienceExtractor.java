package dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class JobRequiredExperienceExtractor
        extends AbstractSingleGptRunner<JobRequiredExperienceExtractor.JobRequiredExperienceResult> {

    protected JobRequiredExperienceExtractor(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, JobRequiredExperienceResult.class);
    }

    @Override
    protected String getSystemPrompt() {
        return PROMPT;
    }

    private static final String PROMPT = """
            Extract required years of experience from the job posting. (minYears, maxYears, experienceRequired) 
            
            
            Follow this 3-step procedure:
            
            1) Scope (narrow the text)
               - Treat as REQUIRED context: "Requirements", "Qualifications", "Minimum/Basic Qualifications", "Who You Are".
               - Treat as PREFERRED (ignore for years): "Preferred", "Nice to have", "Bonus".
               - Ignore sections: "Overview/Description", "About", "Benefits", "Compensation/Salary", "Privacy", "Inclusion & Diversity", "Where You'll Be".
            
            2) Decide experienceRequired (yes/no)
               - If title contains seniority terms ("Senior", "Staff", "Principal", "Lead", "Sr.") → experienceRequired = true.
               - If the posting explicitly says “new grad welcome / no experience required / any experience ”
                 AND it clearly targets multiple levels (“All levels”, “Junior–Senior”, etc.) → experienceRequired = false.
               - If no explicit years found after Step 1 and no seniority signals → experienceRequired = false.
            
            3) Extract years (only if experienceRequired=true and explicit numbers exist)
               - Patterns:
                 • "X–Y years", "between X and Y",  → minYears=X, maxYears=Y
                 • "X+ years", "at least X", "X years or more" → minYears=X, maxYears=null
                 • "Up to Y years" → minYears=null, maxYears=Y
                 • "X years experience" → minYears=X, maxYears=null
               - Units to normalize: "years", "yr", "yrs", "YoE"
               - Do not infer numbers. Ignore salary figures, education phrases, and preferred-only numbers.
            
            Output rules:
            - If experienceRequired=false → minYears=null, maxYears=null.
            - If both minYears and maxYears present and maxYears < minYears → swap.
            - If nothing explicit fits the above → experienceRequired=false, minYears=null, maxYears=null.
            
            
            
        Return **JSON only** in this exact shape:

        {
          "minYears": 3,
          "maxYears": 6,
          "experienceRequired": true
        }
        """;

    public record JobRequiredExperienceResult(
            Integer minYears,
            Integer maxYears,
            Boolean experienceRequired
    ) {}
}
