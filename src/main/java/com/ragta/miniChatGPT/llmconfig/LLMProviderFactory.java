package com.ragta.miniChatGPT.llmconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LLMProviderFactory {

    private final Map<String, IModelProvider> providers;

    @Autowired
    public LLMProviderFactory(Map<String, IModelProvider> providers) {
        this.providers = providers;
    }

    public IModelProvider get(String type) {
        return providers.get(type.toLowerCase());
    }
}
