package dev.devrunner.openai.base;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.util.List;

@Slf4j
public abstract class AbstractListGptRunner<T> implements ListGptRunner<T> {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final TypeReference<List<T>> typeRef;

    protected AbstractListGptRunner(ChatModel chatModel, ObjectMapper objectMapper, TypeReference<List<T>> typeRef) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
        this.typeRef = typeRef;
    }
    /*
    * 생성자 선언은 이렇게.
@Component
@Slf4j
public class BeaminMdParser extends AbstractListGptRunner<BeaminHiringDto> {


    protected BeaminMdParser(ChatModel chatModel, ObjectMapper objectMapper) {
        super(chatModel, objectMapper, new TypeReference<List<BeaminHiringDto>>() {
        });
    }
    * */

    // 수행할 시스템 프롬프트 .
    protected abstract String getSystemPrompt();

    @Override
    public List<T> runAsList(GptParams params) {
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(getSystemPrompt()),
                new UserMessage(params.userPrompt())
        ), OpenAiChatOptions.builder()
                .model(params.model())
                .temperature(0.3)
                .build());

        Generation generation = chatModel.call(prompt).getResult();
        log.info("generation :{}", generation.toString());
        log.info("gen result : \n {}", generation.getOutput().getText());
        String raw = CleanJson.cleanJsonString(generation.getOutput().getText());

        try {
            return objectMapper.readValue(raw, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("리스트 JSON 파싱 오류", e);
        }
    }
}