package com.ragta.miniChatGPT.llmconfig.openai;

import com.ragta.miniChatGPT.configurations.OpenAIConfig;
import com.ragta.miniChatGPT.llmconfig.EmbeddingProvider;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("open-ai-embedding")
public class OpenAIEmbeddingProvider implements EmbeddingProvider {

    private final EmbeddingModel embeddingModel;

    @Autowired
    public OpenAIEmbeddingProvider(OpenAIConfig openAIConfig) {
        this.embeddingModel = OpenAiEmbeddingModel.builder()
                .baseUrl(openAIConfig.getEndpoint())
                .modelName(openAIConfig.getEmbeddingModel())
                .build();
    }

    @Override
    public EmbeddingModel provideEmbeddingModel() {
        return embeddingModel;
    }
}
