package com.ragta.miniChatGPT.configurations;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "minichatgpt.ollama")
public class OllamaConfig {

    private String endpoint;
    private String chatModel;
    private String embeddingModel;
}
