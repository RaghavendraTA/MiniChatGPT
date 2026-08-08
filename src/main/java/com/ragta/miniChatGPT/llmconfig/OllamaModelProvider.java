package com.ragta.miniChatGPT.llmconfig;

import com.ragta.miniChatGPT.configurations.OllamaConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("ollama")
public class OllamaModelProvider implements IModelProvider {

    private final StreamingChatModel streamingChatModel;
    private final ChatModel chatModel;
    private final EmbeddingModel embeddingModel;

    // TODO: Configure timeouts, retries, and backoff.

    @Autowired
    public OllamaModelProvider(OllamaConfig ollamaConfig) {

        this.streamingChatModel = OllamaStreamingChatModel.builder()
                .baseUrl(ollamaConfig.getEndpoint())
                .temperature(0.0)
                .logRequests(true)
                .logResponses(true)
                .modelName(ollamaConfig.getChatModel())
                .build();

        this.chatModel = OllamaChatModel.builder()
                .baseUrl(ollamaConfig.getEndpoint())
                .temperature(0.0)
                .logRequests(true)
                .logResponses(true)
                .modelName(ollamaConfig.getChatModel())
                .build();

        this.embeddingModel = OllamaEmbeddingModel.builder()
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
