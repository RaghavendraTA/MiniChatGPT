package com.ragta.miniChatGPT.llmconfig.ollama;

import com.ragta.miniChatGPT.configurations.OllamaConfig;
import com.ragta.miniChatGPT.llmconfig.EmbeddingProvider;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("ollama-embedding")
public class OllamaEmbeddingProvider implements EmbeddingProvider {

    private final EmbeddingModel embeddingModel;

    @Autowired
    public OllamaEmbeddingProvider(OllamaConfig ollamaConfig) {
        this.embeddingModel = OllamaEmbeddingModel.builder()
                .baseUrl(ollamaConfig.getEndpoint())
                .modelName(ollamaConfig.getEmbeddingModel())
                .build();
    }

    @Override
    public EmbeddingModel provideEmbeddingModel() {
        return embeddingModel;
    }
}
