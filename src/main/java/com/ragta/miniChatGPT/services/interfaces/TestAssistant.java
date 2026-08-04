package com.ragta.miniChatGPT.services.interfaces;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.memory.ChatMemoryAccess;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface TestAssistant extends ChatMemoryAccess {

    static String SYSTEM_MESSAGE = """
            you are a chat agent.
            **Do not mention anything about the provided information**
            **If the provided content doesn't have what user asking then just apologise**
            """;

    @SystemMessage(SYSTEM_MESSAGE)
    TokenStream chat(@MemoryId int memoryId, @UserMessage String userMessage);
}
