package dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail;


import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.model.job.RemotePolicy;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class JobRemotePolicyExtractor
        extends AbstractSingleGptRunner<JobRemotePolicyExtractor.JobRemotePolicyResult> {

    protected JobRemotePolicyExtractor(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, JobRemotePolicyResult.class);
    }

    @Override
    protected String getSystemPrompt() {
        return PROMPT;
    }

    private static final String PROMPT = """
        Read the following job posting and classify its **Remote Work Policy** using these rules:

        - `REMOTE`: clear phrases like "fully remote", "100% remote", "work from home", "remote-first".
        - `HYBRID`: mixed mode such as "office X days per week", "hybrid", "remote + office", "in-person collaboration at times".
        - `ONSITE`: full office presence such as "on-site only", "office-based", "must work at designated office".
        - `UNKNOWN`: no reliable signal or the policy is unclear.

        ❗ Priority: `REMOTE > HYBRID > ONSITE`.
        For example, "1 day at office per week / otherwise remote" should be treated as `REMOTE`.

        Return JSON in this exact shape:

        ```json
        {
          "remotePolicy": "REMOTE"
        }
        ```
        """;

    public record JobRemotePolicyResult(String remotePolicy) {
        public RemotePolicy getRemotePolicyEnum() {
            return RemotePolicy.fromString(remotePolicy);
        }

        public String getRemotePolicy() {
            return remotePolicy;
        }
    }
}