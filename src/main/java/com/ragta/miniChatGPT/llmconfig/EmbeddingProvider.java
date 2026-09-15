package com.ragta.miniChatGPT.llmconfig;

import dev.langchain4j.model.embedding.EmbeddingModel;

public interface EmbeddingProvider {

    EmbeddingModel provideEmbeddingModel();

}
