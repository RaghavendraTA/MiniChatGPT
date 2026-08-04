package com.ragta.miniChatGPT.services;

import com.ragta.miniChatGPT.dtos.TokenResponse;
import com.ragta.miniChatGPT.services.interfaces.TestAssistant;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class ChatService {

    private final TestAssistant assistant;
    private final StreamingChatModel chatModel;

    @Autowired
    public ChatService(DocumentService documentService) {

        this.chatModel = OllamaStreamingChatModel.builder()
                .baseUrl("http://localhost:11434")
                .temperature(0.0)
                .logRequests(true)
                .logResponses(true)
                .modelName("gemma4:e4b")
                .build();

        this.assistant = AiServices.builder(TestAssistant.class)
                .streamingChatModel(chatModel)
                .contentRetriever(documentService.getContentRetriever())
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

    public Flux<TokenResponse> chat(int chatId, String userQuery) {
        Sinks.Many<TokenResponse> sink = Sinks.many().unicast().onBackpressureBuffer();

        assistant.chat(chatId, userQuery)
                .onPartialResponse(token -> sink.tryEmitNext(new TokenResponse(token)))
                .onCompleteResponse(response -> sink.tryEmitComplete())
                .onError(sink::tryEmitError)
                .start();

        return sink.asFlux();
    }
}
