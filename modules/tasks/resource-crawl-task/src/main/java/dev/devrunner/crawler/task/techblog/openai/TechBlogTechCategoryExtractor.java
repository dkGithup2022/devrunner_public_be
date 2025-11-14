package dev.devrunner.crawler.task.techblog.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.model.common.TechCategory;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * TechBlog TechCategory 추출기
 *
 * 기술 블로그 본문을 분석하여 관련 기술 카테고리를 추출합니다.
 * String으로 받아서 TechCategory enum으로 변환하며, 매핑 실패 시 무시합니다.
 */
@Component
public class TechBlogTechCategoryExtractor
        extends AbstractSingleGptRunner<TechBlogTechCategoryExtractor.TechBlogCategoryResult> {

    protected TechBlogTechCategoryExtractor(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, TechBlogCategoryResult.class);
    }

    @Override
    protected String getSystemPrompt() {
        return PROMPT;
    }

    private static final String PROMPT = """
        You are an AI classifier that analyzes tech blog articles and assigns relevant technology categories.

        The predefined categories are:

        🔹 Tech Areas
        BACKEND, FRONTEND, DEVOPS, LLM, MACHINE_LEARNING, DATA_ENGINEERING, NETWORK, SYSTEM_ARCHITECTURE

        🔹 Programming Languages
        JAVA, PYTHON, JAVASCRIPT, TYPESCRIPT, GO, RUST, C_PLUS_PLUS, KOTLIN, C_SHARP, PHP, RUBY, SWIFT, PERL, R

        🔹 Frameworks / Runtimes
        SPRING, NODE_JS, REACT, NEXT_JS, SVELTE, FLUTTER, ANDROID, IOS, ANGULAR, RUBY_ON_RAILS, EXPRESS_JS, LARAVEL, ASP_NET_CORE, NUXT_JS, REACT_NATIVE

        🔹 Data Technologies
        RDMS, REDIS, KAFKA, ELASTICSEARCH, MONGO_DB, NO_SQL, MYSQL, POSTGRESQL, SQLITE, MARIA_DB, ORACLE, DYNAMO_DB, CASSANDRA, COUCHDB, CLICK_HOUSE

        🔹 Cloud & Infrastructure
        AWS, AZURE, GCP, SERVERLESS, DOCKER, K8S, TERRAFORM, ANSIBLE, JENKINS, GITLAB, CIRCLE_CI, CICD

        🔹 AI & ML
        ML, NLP, LLM, LLAMA, OPENAI

        🔹 Other Technologies
        SQL, GIT, NGINX, PROMETHEUS, GRAFANA, WEBSERVER, GITHUB_ACTIONS, FLUENT_BIT

        ---
        Analyze the following tech blog article and select 3 to 6 categories that are MOST relevant
        **only from the list above**.

        Guidelines:
        - Choose from the predefined categories only.
        - Focus on the main technologies, frameworks, and methodologies discussed.
        - If the article covers a broad topic, select representative categories.
        - Prioritize technical keywords over soft topics.
        - Return 3 to 6 categories that best represent the article's content.

        Output JSON in the exact format below:

        ```json
        {
          "categories": ["SPRING", "KAFKA", "BACKEND", "K8S"]
        }
        ```
        """;

    /**
     * TechBlog 카테고리 추출 결과
     *
     * String 리스트로 받아서 TechCategory enum으로 변환
     * 매핑 실패 시 null 반환 후 필터링하여 무시
     */
    public record TechBlogCategoryResult(List<String> categories) {
        /**
         * String 리스트를 TechCategory enum 리스트로 변환
         *
         * @return TechCategory enum 리스트 (매핑 실패한 항목은 제외)
         */
        public List<TechCategory> getTechCategoryEnums() {
            return categories.stream()
                    .map(TechCategory::safeFrom)
                    .filter(Objects::nonNull) // null 제거 (매핑 실패한 항목 무시)
                    .toList();
        }
    }
}
