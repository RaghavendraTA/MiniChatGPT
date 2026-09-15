package com.ragta.miniChatGPT.llmconfig;

import dev.langchain4j.model.chat.StreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMConfig {

    @Bean
    public StreamingChatModel streamingChatModel(LLMProviderFactory factory, @Value("${llm.chat.providers}") String providersCsv) {
        String defaultProvider = providersCsv.split(",")[0].trim().toLowerCase();
        return factory.get(defaultProvider).provideStreamingChatModel();
    }
}