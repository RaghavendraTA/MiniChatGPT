package com.ragta.miniChatGPT.services;

import com.ragta.miniChatGPT.dtos.TokenCompleteResponse;
import com.ragta.miniChatGPT.dtos.TokenContent;
import com.ragta.miniChatGPT.dtos.TokenResponse;
import com.ragta.miniChatGPT.services.interfaces.DietitianAgent;
import com.ragta.miniChatGPT.services.interfaces.TestAssistant;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class ChatService {

    private final TestAssistant assistant;

    @Autowired
    public ChatService(StreamingChatModel streamingChatModel, DocumentService documentService) {
        this.assistant = AiServices.builder(TestAssistant.class)
                .streamingChatModel(streamingChatModel)
                .contentRetriever(documentService.getContentRetriever())
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

    public Flux<TokenResponse> chat(int chatId, String userQuery) {
        Sinks.Many<TokenResponse> sink = Sinks.many().unicast().onBackpressureBuffer();

        assistant.chat(chatId, userQuery)
                .onPartialResponse(token -> sink.tryEmitNext(new TokenContent(token)))
                .onCompleteResponse(response -> {
                    sink.tryEmitNext(new TokenCompleteResponse("done"));
                    sink.tryEmitComplete();
                })
                .onError(sink::tryEmitError)
                .start();

        return sink.asFlux();
    }
}
