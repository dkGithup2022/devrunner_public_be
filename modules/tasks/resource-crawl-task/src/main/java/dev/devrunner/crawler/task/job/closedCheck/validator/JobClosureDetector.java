package dev.devrunner.crawler.task.job.closedCheck.validator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 채용 공고 마감 상태 감지기 (2단계)
 * <p>
 * GPT를 사용하여 채용 공고의 지원 가능 상태(OPEN/CLOSED/UNKNOWN)를 판별합니다.
 * 1단계(JobContentValidator)에서 유효한 채용 공고로 확인된 경우에만 사용합니다.
 */
@Component
public class JobClosureDetector extends AbstractSingleGptRunner<JobClosureDetector.DetectionResult> {

    protected JobClosureDetector(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, DetectionResult.class);
    }

    @Override
    protected String getSystemPrompt() {
        return PROMPT;
    }

    public static final String PROMPT = """
            역할: Firecrawl 원문에서 **지원 가능 상태**를 판별한다. 출력은 JSON.

            형식:
            {"state":"OPEN|CLOSED|UNKNOWN","reason":"still_open|open_until_filled|deadline_passed|explicit_closed|insufficient_info|conflicting_signals"}

            규칙(KST 기준, nowKST 사용):
            1) 명시적 마감 문구 → CLOSED(explicit_closed).
               예: "마감됨/모집 마감/지원 종료/영입 종료/접수 마감/지원서 접수 완료", "closed/expired/no longer accepting applications".
            2) 마감일 파싱(예: "2025년 9월 9일 오후 11시 59분까지") → KST ISO8601.
               - deadline < nowKST → CLOSED(deadline_passed)
               - deadline ≥ nowKST → OPEN(still_open)
            3) "수시채용/영입 완료 시 마감" 등 날짜·마감문구 없음 → OPEN(open_until_filled).
            4) 신호 충돌(본문은 마감, 헤더는 모집중 등) → UNKNOWN(conflicting_signals).
            5) 정보 부족 → UNKNOWN(insufficient_info).
            반드시 한 줄 JSON만 반환.

            [User]
            nowKST: {{ISO8601_NOW_IN_KST}}
            title: {{PAGE_TITLE_OR_EMPTY}}
            url: {{SOURCE_URL_OR_EMPTY}}
            content: |
            {{RAW_MARKDOWN_OR_PLAIN_TEXT_CONTENT}}

            출력 예시(형식 참고):
            ```
            {"state":"CLOSED","reason":"deadline_passed"}
            ```
        """;

    /**
     * 사용자 프롬프트 생성 (nowKST, title, url, content 포함)
     */
    public String buildUserPrompt(String markdown, String title, String url) {
        ZonedDateTime nowKST = ZonedDateTime.now(java.time.ZoneId.of("Asia/Seoul"));
        String isoNowKST = nowKST.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        return String.format("""
                nowKST: %s
                title: %s
                url: %s
                content: |
                %s
                """,
                isoNowKST,
                title != null ? title : "",
                url != null ? url : "",
                markdown
        );
    }

    /**
     * 채용 공고 마감 상태 감지 결과
     */
    public record DetectionResult(
            @JsonProperty("state")
            String state,  // OPEN, CLOSED, UNKNOWN

            @JsonProperty("reason")
            String reason  // still_open, open_until_filled, deadline_passed, explicit_closed, insufficient_info, conflicting_signals
    ) {
    }
}
