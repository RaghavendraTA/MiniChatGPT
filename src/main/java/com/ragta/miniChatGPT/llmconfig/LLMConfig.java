package com.ragta.miniChatGPT.llmconfig;

import dev.langchain4j.model.chat.StreamingChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMConfig {

    @Bean
    public StreamingChatModel streamingChatModel(LLMProviderFactory factory) {
        return factory.get("ollama").provideStreamingChatModel();
    }
}