package dev.devrunner.crawler.task.job.closedCheck.validator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

/**
 * 채용 공고 콘텐츠 형식 검증기 (1단계)
 * <p>
 * GPT를 사용하여 크롤링된 콘텐츠가 단일 포지션 채용 공고 형식인지 검증합니다.
 * 오픈/마감 상태는 판단하지 않으며, 형식만 검증합니다.
 */
@Component
public class JobContentValidator extends AbstractSingleGptRunner<JobContentValidator.ValidationResult> {

    protected JobContentValidator(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, ValidationResult.class);
    }

    @Override
    protected String getSystemPrompt() {
        return PROMPT;
    }

    public static final String PROMPT = """
            역할: 입력된 마크다운/원문 문자열이 **단일 포지션 채용 공고 형식**인지 여부만 판별한다.
                   - 사이트 네비/관련 공고/회사 공통 소개/푸터 등은 판단에서 제외한다.
                   - **오픈/마감/마감일/링크 접근성** 등 상태나 품질 판단은 하지 않는다(다른 단계에서 처리).
                   - 다수 포지션 **목록/검색결과**만 있는 경우는 채용 공고 형식이 아니므로 false.

                   채용 공고 형식으로 보는 신호(아래 중 **2개 이상** 확인되면 true):
                   - 포지션/직무 제목(예: "백엔드 개발자", "ML Engineer" 등)
                   - 담당 업무 / Responsibilities / Role
                   - 자격 요건 / Qualifications / Preferred
                   - 지원/지원 방법 / Apply / 전형 절차
                   - 근무지 / 고용 형태(정규/계약/인턴/리모트 등)
                   - 복지/혜택/근무 제도(benefits) (보조 신호)

                   false로 보는 명확한 경우(예시):
                   - 단순 **목록/카테고리/검색 페이지**로 보이며 개별 포지션 본문이 없음
                   - 에러/리디렉션/접근 안내만 있음(404/오류/다시 시도 등)
                   - 회사 홍보/블로그/뉴스/FAQ/CSR 안내 등 **채용 안내가 아닌** 일반 콘텐츠

                   출력: **정확히 한 줄 JSON**만.
                   ```
                   {"validHiringContent": true}
                   ```

                   또는
                   ```
                   {"validHiringContent": false}
                    ```
        """;

    /**
     * 채용 공고 콘텐츠 형식 검증 결과
     */
    public record ValidationResult(
            @JsonProperty("validHiringContent")
            boolean validHiringContent
    ) {
    }
}
