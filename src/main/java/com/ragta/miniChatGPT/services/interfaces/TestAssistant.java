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
            you are a chat agent, answer using provided data.
            If provided information is irrelevant then use intelligence to answer
            **Do not mention anything about the provided information**
            """;

    @SystemMessage(SYSTEM_MESSAGE)
    TokenStream chat(@MemoryId int memoryId, @UserMessage String userMessage);
}
