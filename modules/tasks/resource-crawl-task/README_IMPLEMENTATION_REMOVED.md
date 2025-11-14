# Resource Crawl Task Module

> **📝 안내:** 이 모듈은 프로덕션의 모든 부분을 공개하지 않습니다.

## 제거된 부분

1. **실험 목적의 API와 기능 제거**
2. **HTML을 직접 다루는 부분 제거**
3. **테스트 코드 제거**

## 제거한 이유

현재 크롤링 및 공개 대상은 **Terms of Use에서 비상업적 사용에 대한 명시가 없는 자료**에 한해서 가져와서 제공 중입니다.

적어도 제가 제휴를 맺기 전까지는 상업적 사용은 없을 예정입니다.

그래도, **법적으로 애매하게 얽힌 부분인 HTML에 대한 직접 파싱을 대중에 공개할 수는 없을 것 같습니다.**

---

## 이 문서에서 보여드리고 싶은 것

저는 이 문서를 통해 다음을 공유하고자 합니다:

1. **저가형 모델을 통한 정보 추출 방법론**
   - GPT-4o-mini 같은 작고 저렴한 모델로도 충분히 정확한 데이터 추출 가능
   - 20개의 독립적인 Extractor로 역할 분리하여 각각 최적화

2. **반복적인 LLM 호출 최적화**
   - 각 Extractor는 단일 책임만 수행 (Single Responsibility)
   - Prompt를 명확하게 작성하여 정확도 향상

3. **Context 크기 최적화**
   - 긴 채용 공고를 그대로 GPT에 넣으면 Token 낭비 + 비용 증가
   - Content Shortener로 불필요한 부분 제거 후 분석

**주요 인터페이스와 Prompt가 공개되며, 테스트와 구현체 부분은 제거 후 공개합니다.**

---

이 모듈은 **private 레포지토리에서 완전히 동작하며 프로덕션에서 사용 중**입니다.

---

## 이 모듈이 하는 일

### 전체 파이프라인

```
[1단계] URL 수집
   ↓
[2단계] Content 크롤링 (HTML → Markdown)
   ↓
[3단계] GPT 분석 (Markdown → Structured Data)
   ↓
[4단계] DB 저장 & Elasticsearch 인덱싱
```

### 처리 대상

#### 1. 채용 공고 (Job Postings)
- **수집 대상**: Google, Meta, Netflix, Spotify, TikTok, Naver, Line, Woowahan, Karrot 등
- **추출 정보**: 포지션, 기술 스택, 경력, 위치, 급여, 채용 프로세스 등 20개 필드
- **특징**: 각 회사별 채용 페이지 구조가 달라서 개별 파서 구현

#### 2. 기술 블로그 (Tech Blogs)
- **수집 대상**: 주요 IT 기업 공식 기술 블로그
- **추출 정보**: 요약, 기술 카테고리, 한 줄 소개
- **특징**: RSS 또는 페이지 크롤링

---

## 아키텍처 개요

### 1단계: URL 수집

**목적**: 크롤링할 채용 공고/블로그 URL 목록 생성

**방식**:
- 채용 공고: 각 회사 채용 페이지 리스트 페이지에서 URL 추출
- 기술 블로그: RSS 피드 또는 목록 페이지 파싱

**구현 (제거됨)**:
- `UrlListParser` 인터페이스 구현체들 (Google, Meta, Naver 등)
- Playwright/Jsoup 기반 HTML 파싱

---

### 2단계: Content 크롤링

**목적**: URL에서 실제 콘텐츠를 추출하여 Markdown으로 변환

**사용 도구**:
- **Firecrawl API**: HTML을 Markdown으로 변환 (유료 API)
- **Playwright**: 동적 페이지 렌더링 필요 시

**Firecrawl API 인터페이스** (개념만):
```java
public interface FireCrawlerApi {
    /**
     * URL을 크롤링하여 Markdown으로 변환
     * @param url 크롤링 대상 URL
     * @return Markdown 형식의 콘텐츠
     */
    String scrapeToMarkdown(String url);
}
```

**Playwright API 인터페이스** (개념만):
```java
public interface PlaywrightApi {
    /**
     * 동적 페이지를 렌더링하여 HTML 추출
     * @param url 대상 URL
     * @param waitTimeMs 로딩 대기 시간
     * @return 렌더링된 HTML
     */
    String renderAndGetHtml(String url, int waitTimeMs);
}
```

**Content Shortener**:
- Markdown이 너무 길면 GPT Token 제한 초과
- 불필요한 부분(네비게이션, 푸터 등) 제거
- 회사별로 커스텀 로직 적용 (구현 제거됨)

---

### 3단계: GPT 분석 (핵심!)

**목적**: 비구조화된 Markdown을 구조화된 JSON 데이터로 변환

이 단계가 **이 시스템의 핵심 노하우**입니다. 20개의 정교한 GPT Prompt를 통해 채용 공고를 분석합니다.

#### 추상 클래스: AbstractSingleGptRunner

모든 Extractor의 베이스 클래스입니다:

```java
package dev.devrunner.openai.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.model.ChatModel;

/**
 * GPT 기반 단일 추출기 추상 클래스
 *
 * @param <T> 추출할 데이터 타입
 */
public abstract class AbstractSingleGptRunner<T> {

    protected final ChatModel chatModel;
    protected final ObjectMapper objectMapper;
    protected final Class<T> resultType;

    protected AbstractSingleGptRunner(ChatModel chatModel,
                                     ObjectMapper objectMapper,
                                     Class<T> resultType) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
        this.resultType = resultType;
    }

    /**
     * 각 Extractor가 구현해야 하는 System Prompt
     */
    protected abstract String getSystemPrompt();

    /**
     * GPT를 호출하여 데이터 추출
     * @param userInput 분석 대상 텍스트 (Markdown)
     * @return 추출된 데이터
     */
    public T extract(String userInput) {
        // Spring AI를 사용하여 GPT 호출
        // System Prompt + User Input → JSON Response → T 객체로 변환
        // (구현 생략)
    }
}
```

---

## GPT Prompt 모음 (20개)

### 1. JobSummaryExtractor - 핵심 키워드 추출

**목적**: 채용 공고에서 기술 키워드와 비즈니스 맥락을 10-15개 추출

```java
@Component
public class JobSummaryExtractor extends AbstractSingleGptRunner<String> {

    @Override
    protected String getSystemPrompt() {
        return """
                Job Posting Keyword & Purpose Extractor

                Analyze the following job posting and extract 10–15 concise keyword phrases
                that summarize the main technical topics and their purposes or business contexts.

                Each phrase should naturally express what the technology or method is used for,
                and — when possible — hint at the related business domain (e.g., payment, logistics, commerce).

                ## Guidelines

                - Focus on core technical concepts, architectures, or workflows mentioned in the job description.
                - Include purpose or business context (e.g., improve reliability, reduce latency, automate logistics, optimize payment flow).
                - Avoid company names, numbers, or generic terms.
                - Use English only and keep phrases short (3–7 words).
                - Aim for 10–15 phrases total.
                - Use exact terminology from the posting when possible.

                ## EXAMPLE
                ```
                Backend Engineer tasks using Java, Spring
                JVM tuning for performance optimization
                Kafka streaming for real-time data processing
                Elasticsearch indexing for efficient retrieval
                Payment pipeline design for fraud prevention
                Logistics routing for delivery optimization
                Recommendation modeling for commerce growth
                Ad bidding system for campaign performance
                ```
                """;
    }
}
```

---

### 2. JobPositionCategoryExtractor - 포지션 분류

**목적**: 채용 공고를 10개 카테고리로 분류

```java
@Component
public class JobPositionCategoryExtractor
        extends AbstractSingleGptRunner<JobPositionCategoryResult> {

    @Override
    protected String getSystemPrompt() {
        return """
        Read the following job posting and classify the **Position Category** as exactly ONE of:

        - `BACKEND`: server-side APIs, databases, systems architecture
        - `FRONTEND`: web UI/UX, web application development
        - `FULLSTACK`: both frontend and backend responsibilities
        - `MOBILE`: Android, iOS, or cross-platform (e.g., Flutter)
        - `DATA`: data pipelines, analytics/BI, DWH engineering
        - `ML_AI`: machine learning models, LLM applications, AI modeling
        - `DEVOPS`: cloud infrastructure, CI/CD, observability/operations
        - `HARDWARE`: embedded systems, chipsets, electronics
        - `QA`: quality assurance, test engineering, QA ops
        - `NOT_CATEGORIZED`: use only if none of the above clearly applies

        Guidelines:
        - Even if multiple technologies are mentioned, choose the SINGLE most central role.
        - If responsibilities are vague, make the best determination based on the core duties.
        - Use `NOT_CATEGORIZED` only when a clear mapping is impossible.

        Return JSON in this exact shape:

        ```json
        {
          "positionCategory": "DEVOPS"
        }
        ```
        """;
    }

    public record JobPositionCategoryResult(String positionCategory) {}
}
```

---

### 3. JobTechCategoryExtractor - 기술 스택 분류

**목적**: 관련 기술 스택을 미리 정의된 카테고리에서 0-3개 선택

```java
@Component
public class JobTechCategoryExtractor
        extends AbstractSingleGptRunner<JobTechCategoryResult> {

    @Override
    protected String getSystemPrompt() {
        return """
        You are an AI classifier that analyzes job postings and assigns relevant technology categories.

        The predefined categories are:

        🔹 Tech Areas
        BACKEND, FRONTEND, DEVOPS, LLM, MACHINE_LEARNING, DATA_ENGINEERING, NETWORK, SYSTEM_ARCHITECTURE

        🔹 Programming Languages
        JAVA, PYTHON, JAVASCRIPT, TYPESCRIPT, GO, RUST, C_PLUS_PLUS, KOTLIN

        🔹 Frameworks / Runtimes
        SPRING, NODE_JS, REACT, NEXT_JS, SVELTE, FLUTTER, ANDROID, IOS

        🔹 Data Technologies
        RDMS, REDIS, KAFKA, ELASTICSEARCH, MONGO_DB, NO_SQL

        ---
        Analyze the following job description and select 0 to 3 categories that are MOST relevant
        **only from the list above**.

        Guidelines:
        - Choose from the predefined categories only.
        - If the description is broad, focus on the core technical keywords.
        - If there is no clearly related technology, return an empty array.
        - Ignore soft skills, hiring process, and team culture.

        Output JSON in the exact format below:

        ```json
        {
          "categories": ["SPRING", "RDMS", "BACKEND"]
        }
        ```
        """;
    }

    public record JobTechCategoryResult(List<String> categories) {}
}
```

---

### 4. JobRequiredExperienceExtractor - 경력 요구사항 추출

**목적**: 최소/최대 경력 연수와 경력 필수 여부 추출

```java
@Component
public class JobRequiredExperienceExtractor
        extends AbstractSingleGptRunner<JobRequiredExperienceResult> {

    @Override
    protected String getSystemPrompt() {
        return """
            Extract required years of experience from the job posting. (minYears, maxYears, experienceRequired)


            Follow this 3-step procedure:

            1) Scope (narrow the text)
               - Treat as REQUIRED context: "Requirements", "Qualifications", "Minimum/Basic Qualifications", "Who You Are".
               - Treat as PREFERRED (ignore for years): "Preferred", "Nice to have", "Bonus".
               - Ignore sections: "Overview/Description", "About", "Benefits", "Compensation/Salary", "Privacy", "Inclusion & Diversity", "Where You'll Be".

            2) Decide experienceRequired (yes/no)
               - If title contains seniority terms ("Senior", "Staff", "Principal", "Lead", "Sr.") → experienceRequired = true.
               - If the posting explicitly says "new grad welcome / no experience required / any experience "
                 AND it clearly targets multiple levels ("All levels", "Junior–Senior", etc.) → experienceRequired = false.
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
    }

    public record JobRequiredExperienceResult(
            Integer minYears,
            Integer maxYears,
            Boolean experienceRequired
    ) {}
}
```

---

### 5-20. 나머지 Extractor들

**구현되어 있는 추가 Extractor들** (Prompt는 private 레포에만):

#### Job 정보 추출
- `JobLocationExtractor`: 근무지 추출
- `JobRemotePolicyExtractor`: 재택 정책 (Remote/Hybrid/Onsite)
- `JobEmploymentTypeExtractor`: 고용 형태 (정규직/계약직 등)
- `JobOrganizationExtractor`: 부서/팀 정보
- `JobDateExtractor`: 공고 게시일/마감일
- `JobOneLineSummaryExtractor`: 한 줄 요약

#### Job 상세 설명 추출
- `JobPositionIntroductionExtractor`: 포지션 소개
- `JobResponsibilitiesExtractor`: 주요 업무
- `JobPositionRequirementsExtractor`: 자격 요건

#### Job 채용 프로세스
- `JobHiringProcessExtractor`: 채용 절차
- `JobInterviewStepsExtractor`: 면접 단계

#### Job 급여 정보
- `JobPayExtractor`: 급여 정보 유무
- `JobPayDetailExtractor`: 구체적 급여 금액

#### TechBlog 분석
- `TechBlogSummarizer`: 기술 블로그 요약
- `TechBlogOneLinerExtractor`: 한 줄 소개
- `TechBlogTechCategoryExtractor`: 기술 카테고리
- `TechBlogCategoryExtractor`: 블로그 카테고리
- `TechBlogKoreanSummaryTranslator`: 한글 번역

---

## 데이터 모델

### 크롤링 중간 데이터 (DB 테이블)

```sql
-- 1단계: URL 수집 결과 저장
CREATE TABLE crawl_job_url (
    id BIGINT PRIMARY KEY,
    url VARCHAR(2048) NOT NULL,
    company VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,  -- PENDING, COMPLETED, FAILED
    created_at TIMESTAMP NOT NULL
);

-- 2단계: Content 크롤링 결과 저장
CREATE TABLE crawl_job_content (
    id BIGINT PRIMARY KEY,
    url VARCHAR(2048) NOT NULL,
    markdown_content TEXT,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- 마감된 공고 체크
CREATE TABLE job_closed_check (
    id BIGINT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    is_closed BOOLEAN NOT NULL,
    closed_reason VARCHAR(255),
    checked_at TIMESTAMP NOT NULL
);
```

### 최종 데이터 (Job 모델)

GPT 분석 결과는 `Job` 도메인 모델로 저장되며, 이후 Elasticsearch로 동기화됩니다.

```java
// 개념적 구조 (실제는 더 복잡함)
public class Job {
    private Long jobId;
    private String company;
    private String title;
    private String url;

    // GPT 추출 정보
    private PositionCategory positionCategory;  // BACKEND, FRONTEND, etc.
    private List<TechCategory> techCategories;   // SPRING, JAVA, KAFKA, etc.
    private String summary;                      // 10-15 keywords
    private String oneLiner;                     // 한 줄 요약

    private Integer minYears;
    private Integer maxYears;
    private Boolean experienceRequired;

    private String location;
    private String remotePolicy;               // REMOTE, HYBRID, ONSITE
    private String employmentType;             // FULL_TIME, CONTRACT, etc.

    // 상세 설명
    private String positionIntroduction;
    private String responsibilities;
    private String requirements;

    // 채용 프로세스
    private String hiringProcess;
    private String interviewSteps;

    // 급여
    private Boolean payInfoAvailable;
    private String payDetail;

    // 메타 정보
    private LocalDateTime createdAt;
    private LocalDateTime deadline;
    private Boolean isClosed;
}
```

---

## 4단계: DB 저장 & Elasticsearch 동기화

**DB 저장**:
- GPT 분석 결과를 `Job` 테이블에 저장
- Spring Data JDBC 사용

**Elasticsearch 동기화**:
- Outbox 패턴 사용
- `elasticsearch-sync-task` 모듈에서 배치로 동기화
- 검색 최적화를 위한 인덱싱

---

## 배치 스케줄링

Spring Batch 기반으로 주기적 실행:

```java
@Scheduled(cron = "0 0 2 * * ?")  // 매일 새벽 2시
public void crawlDailyJobs() {
    // 1. URL 수집
    // 2. Content 크롤링
    // 3. GPT 분석
    // 4. DB 저장
}

@Scheduled(cron = "0 0 4 * * ?")  // 매일 새벽 4시
public void checkClosedJobs() {
    // 기존 공고 마감 여부 체크
}
```

---

## 기술 스택

- **Language**: Java 21
- **Framework**: Spring Boot, Spring Batch
- **AI**: Spring AI + OpenAI GPT-4
- **Crawling**: Firecrawl API, Playwright, Jsoup
- **Database**: MySQL (Spring Data JDBC)
- **Search**: Elasticsearch

---

## 확장 가능성

### 새로운 크롤링 대상 추가

1. `UrlListParser` 구현 (URL 수집)
2. `JobContentShortener` 구현 (Content 정제)
3. 배치 Job에 추가

### GPT Prompt 개선

각 Extractor의 `getSystemPrompt()` 메서드를 수정하여 정확도 향상 가능.

**A/B 테스트 예시**:
```java
// Prompt Version A vs Version B 비교
// 실제 데이터로 정확도 측정 후 더 나은 버전 선택
```

---

## 한계 및 개선 방향

### 현재 한계
1. **크롤링 안정성**: 대상 사이트 구조 변경 시 파서 수정 필요
2. **GPT 비용**: 공고 1개당 20번 API 호출 (비용 상승)
3. **Token 제한**: 긴 공고는 Content Shortener로 줄여야 함

### 개선 방향
1. **Prompt 통합**: 20개 → 5개로 줄여서 비용 절감
2. **Fine-tuning**: GPT-4 대신 Fine-tuned 모델 사용
3. **캐싱**: 동일 공고 재분석 방지

---

## Notes

1. **Firecrawl API**: 유료 서비스 (월 구독)
2. **OpenAI GPT-4**: API 키 필요 (환경 변수)
3. **Playwright**: Chromium 다운로드 필요 (`npx playwright install chromium`)
4. **크롤링 윤리**: robots.txt 준수, Rate Limiting 적용
