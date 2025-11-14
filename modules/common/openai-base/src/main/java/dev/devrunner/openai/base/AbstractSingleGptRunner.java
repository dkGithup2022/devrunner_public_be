package dev.devrunner.openai.base;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.util.List;

@Slf4j
public abstract class AbstractSingleGptRunner<T> implements SingleGptRunner<T> {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final ObjectMapper tolerantMapper;  // 내부 전용 관용 매퍼 (fallback)
    private final Class<T> type;

    protected AbstractSingleGptRunner(ChatModel chatModel, ObjectMapper objectMapper, Class<T> type) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
        this.type = type;

        this.tolerantMapper = JsonMapper.builder()
                .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER) // \[ 같은 비표준 이스케이프 허용
                .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)          // 비이스케이프 제어문자 허용
                // 필요 시: .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
                .build();
    }

    protected abstract String getSystemPrompt();

    @Override
    public T run(GptParams params) {
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(getSystemPrompt()),
                new UserMessage(params.userPrompt())
        ), OpenAiChatOptions.builder()
                .model(params.model())
                .temperature(0.3)
                .build());


        Generation generation = chatModel.call(prompt).getResult();
        String raw = CleanJson.cleanJsonString(generation.getOutput().getText());


        try {
            if (type.equals(String.class)) {
                return (T) raw;
            }

            return safeObjectMapper(raw);

        } catch (RuntimeException | JsonProcessingException e) {
            log.info("raw response : {} ", raw);
            throw new RuntimeException("단일값 JSON 파싱 오류", e);
        }
    }

    private T safeObjectMapper(String raw) throws JsonProcessingException {
        try {
            return objectMapper.readValue(raw, type);
        } catch (JsonProcessingException e) {
            log.error("단일값 JSON 파싱 오류 - 주입받은 매퍼 ", e);
            log.error("raw response : {} ", raw);
            return tolerantMapper.readValue(raw, type);
        }
    }
}