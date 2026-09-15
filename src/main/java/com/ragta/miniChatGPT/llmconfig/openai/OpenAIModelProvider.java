package com.ragta.miniChatGPT.llmconfig.openai;

import com.ragta.miniChatGPT.configurations.OllamaConfig;
import com.ragta.miniChatGPT.configurations.OpenAIConfig;
import com.ragta.miniChatGPT.llmconfig.IModelProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import dev.langchain4j.model.openai.*;

@Component("open-ai")
public class OpenAIModelProvider implements IModelProvider {

    private final StreamingChatModel streamingChatModel;
    private final ChatModel chatModel;
    private final EmbeddingModel embeddingModel;

    @Autowired
    public OpenAIModelProvider(OpenAIConfig openAIConfig, OllamaConfig ollamaConfig) {

        this.streamingChatModel = OpenAiStreamingChatModel.builder()
                .baseUrl(openAIConfig.getEndpoint())
                .temperature(0.0)
                .logRequests(true)
                .logResponses(true)
                .modelName(openAIConfig.getChatModel())
                .build();

        this.chatModel = OpenAiChatModel.builder()
                .baseUrl(openAIConfig.getEndpoint())
                .temperature(0.0)
                .logRequests(true)
                .logResponses(true)
                .modelName(openAIConfig.getChatModel())
                .build();

        this.embeddingModel = OpenAiEmbeddingModel.builder()
                .baseUrl(ollamaConfig.getEndpoint())
                .modelName(ollamaConfig.getEmbeddingModel())
                .build();
    }

    @Override
    public EmbeddingModel provideEmbeddingModel() {
        return embeddingModel;
    }

    @Override
    public ChatModel provideChatModel() {
        return chatModel;
    }

    @Override
    public StreamingChatModel provideStreamingChatModel() {
        return streamingChatModel;
    }
}
