package dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail.pay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.model.job.CompensationUnit;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class JobPayDetailExtractor
        extends AbstractSingleGptRunner<JobPayDetailExtractor.JobPayDetailResult> {

    protected JobPayDetailExtractor(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, JobPayDetailResult.class);
    }

    @Override
    protected String getSystemPrompt() { return PROMPT; }

    public static final String PROMPT = """
        You receive ONLY the compensation/pay section of a job post.
        Extract fields below and return JSON ONLY with these exact keys:

        - "min-basepay": number|null          // annual base lower bound (posting currency)
        - "max-basepay": number|null          // annual base upper bound (posting currency)
        - "currency": string                  // "$" => "USD"
        - "unit": "YEARLY"|"HOURLY"           // if annual present → YEARLY; otherwise HOURLY
        - "hasStockOption": true|false
        - "salaryNote": string                // 1–2 sentences; concise, accurate summary of the comp structure

        Rules:
        - If BOTH annual and hourly are present: prefer ANNUAL, set unit="YEARLY", and fill min/max from annual.
        - If ONLY hourly appears: set unit="HOURLY" and min/max=null.
        - Equity/RSU/stock options ⇒ hasStockOption=true.
        - If nothing detectable: set numbers=null, hasStockOption=false, currency="USD", unit="YEARLY",
          salaryNote="Compensation not stated."
        - Output JSON only. No code fences, no prose.

        Example:
        {"min-basepay":141000,"max-basepay":202000,"currency":"USD","unit":"YEARLY","hasStockOption":true,"salaryNote":"Annual base range; bonus, equity and benefits provided."}
        """;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JobPayDetailResult(
            @JsonProperty("min-basepay") BigDecimal minBasepay,
            @JsonProperty("max-basepay") BigDecimal maxBasepay,
            String currency,
            CompensationUnit unit,           // YEARLY | HOURLY
            Boolean hasStockOption,
            String salaryNote
    ) {}
}