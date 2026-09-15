package com.ragta.miniChatGPT.llmconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EmbeddingProviderFactory {

    private final Map<String, EmbeddingProvider> providers;

    @Autowired
    public EmbeddingProviderFactory(Map<String, EmbeddingProvider> providers) {
        this.providers = providers;
    }

    public EmbeddingProvider get(String type) {
        if (type == null) return null;
        EmbeddingProvider p = providers.get(type.toLowerCase());
        if (p == null) {
            p = providers.get(type.toLowerCase() + "-embedding");
        }
        return p;
    }
}
