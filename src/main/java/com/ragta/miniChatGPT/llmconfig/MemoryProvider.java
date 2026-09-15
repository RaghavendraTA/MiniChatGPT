package com.ragta.miniChatGPT.llmconfig;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class MemoryProvider {

    private final int maxMessages = 10;
    private final ConcurrentHashMap<Object, ChatMemory> memoryRegistry = new ConcurrentHashMap<>();

    public ChatMemory getMemoryProvider(Object memoryId) {
        return memoryRegistry.computeIfAbsent(memoryId, id ->
                MessageWindowChatMemory.builder()
                        .maxMessages(maxMessages)
                        .build()
        );
    }
}
