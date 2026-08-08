package com.ragta.miniChatGPT.llmconfig;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

public interface IModelProvider {

    EmbeddingModel provideEmbeddingModel();

    ChatModel provideChatModel();

    StreamingChatModel provideStreamingChatModel();
}
