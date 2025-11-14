package dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail;


import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.model.common.TechCategory;
import dev.devrunner.openai.base.AbstractSingleGptRunner;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JobTechCategoryExtractor
        extends AbstractSingleGptRunner<JobTechCategoryExtractor.JobTechCategoryResult> {

    protected JobTechCategoryExtractor(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, JobTechCategoryResult.class);
    }

    @Override
    protected String getSystemPrompt() {
        return PROMPT;
    }

    private static final String PROMPT = """
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

    public record JobTechCategoryResult(List<String> categories) {
        public List<TechCategory> getTechCategoryEnums() {
            List<TechCategory> techCategoryEnums = new ArrayList<>();
            for (var category : categories) {
                techCategoryEnums.add(TechCategory.safeFrom(category));
            }
            return techCategoryEnums;
        }
    }
}