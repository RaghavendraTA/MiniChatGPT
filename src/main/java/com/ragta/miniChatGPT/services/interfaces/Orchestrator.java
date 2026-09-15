package com.ragta.miniChatGPT.services.interfaces;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface Orchestrator {

    String SYSTEM_MESSAGE = """
            you are a orchestration agent that routes or handoffs to other agents or call functions based
            on the below criteria
            
            - If user is providing personal information call function `user_info`
            - If user asking about what to eat next call function `dietitian`
            - If user asking about what to workout today call function `workout_planner`
            """;

    @SystemMessage(SYSTEM_MESSAGE)
    TokenStream chat(@MemoryId int memoryId, @UserMessage String userMessage);
}
