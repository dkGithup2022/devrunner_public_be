package dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail.explanation;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobPositionRequirementsExtractor
        extends AbstractSingleGptRunner<JobPositionRequirementsExtractor.JobPositionRequirementsResult> {

    public record JobPositionRequirementsResult(
            List<String> qualifications,
            List<String> preferredQualifications
    ) {}

    protected JobPositionRequirementsExtractor(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, JobPositionRequirementsResult.class);
    }

    @Override
    protected String getSystemPrompt() {
        return PROMPT;
    }

    private static final String PROMPT = """
        From the following job posting, extract **Required Qualifications** and **Preferred Qualifications**.

        Rules:
        - **Required Qualifications** are must-have criteria to perform the role.
          - Examples: "백엔드 개발 3년 이상", "Java 및 Spring 능숙", "CI/CD 경험"
        - **Preferred Qualifications** are nice-to-have signals explicitly listed as preferences (e.g., "우대사항," "선호," "plus").
          - Examples: "MSA 운영 경험", "SwiftUI 경험", "오픈소스 기여"
        - If a criterion appears in both areas, **keep it in Required** and omit from Preferred.
        - Exclude company culture, team traits, benefits, and hiring process details.
        - Exclude **soft skills** (e.g., perseverance, passion, communication skills, self-driven, growth mindset).
        - Return **at most 6 items per list**.
        - De-duplicate items and keep each bullet **concise** (phrase form, not full sentences).
        - Do **not** invent information; extract only what is stated.
        - **Output in Korean language**.

        Return **JSON only** in this exact shape:

        {
          "qualifications": [
            "Java 및 Spring 백엔드 개발",
            "MySQL 또는 PostgreSQL 경험",
            "CI/CD 파이프라인 운영"
          ],
          "preferredQualifications": [
            "MSA 프로덕션 운영 경험",
            "Elasticsearch 사용 경험",
            "오픈소스 기여 경험"
          ]
        }
        """;
}