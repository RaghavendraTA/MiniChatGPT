package com.ragta.miniChatGPT.services;

import com.ragta.miniChatGPT.dtos.TokenCompleteResponse;
import com.ragta.miniChatGPT.dtos.TokenContent;
import com.ragta.miniChatGPT.dtos.TokenResponse;
import com.ragta.miniChatGPT.services.interfaces.DietitianAgent;
import com.ragta.miniChatGPT.services.interfaces.TestAssistant;
import com.ragta.miniChatGPT.llmconfig.LLMProviderFactory;
import com.ragta.miniChatGPT.llmconfig.IModelProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ChatService {

        private final LLMProviderFactory providerFactory;
        private final DocumentService documentService;
        private final List<String> providerOrder;
        private final int maxRetries;
        private final long backoffBaseMs;
        private final ThreadPoolExecutor executor;

        @Autowired
        public ChatService(LLMProviderFactory providerFactory,
                   DocumentService documentService,
                   @Value("${llm.providers:ollama}") String providersCsv,
                   @Value("${llm.maxRetries:2}") int maxRetries,
                   @Value("${llm.backoffMs:500}") long backoffBaseMs,
                   @Value("${llm.maxConcurrency:20}") int maxConcurrency,
                   @Value("${llm.queueSize:200}") int queueSize) {

        this.providerFactory = providerFactory;
        this.documentService = documentService;
        this.providerOrder = Arrays.stream(providersCsv.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(String::toLowerCase)
            .collect(Collectors.toList());

        this.maxRetries = Math.max(0, maxRetries);
        this.backoffBaseMs = Math.max(50, backoffBaseMs);

        // Bounded executor to limit concurrent streaming connections and protect resources
        this.executor = new ThreadPoolExecutor(
            maxConcurrency, // core pool size
            maxConcurrency, // max pool size
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(queueSize),
            new ThreadPoolExecutor.CallerRunsPolicy());
        }

    public Flux<TokenResponse> chat(int chatId, String userQuery) {
        Sinks.Many<TokenResponse> sink = Sinks.many().unicast().onBackpressureBuffer();

        // Submit the streaming work to the bounded executor so many concurrent requests
        // stay under control and don't exhaust system resources.
        executor.submit(() -> {
            Exception lastException = null;

            // Try providers in configured order, with retries and exponential backoff per provider
            for (String providerName : providerOrder) {
                IModelProvider modelProvider = providerFactory.get(providerName);
                if (modelProvider == null) continue;

                for (int attempt = 0; attempt <= maxRetries; attempt++) {
                    try {
                        TestAssistant assistant = AiServices.builder(TestAssistant.class)
                                .streamingChatModel(modelProvider.provideStreamingChatModel())
                                .contentRetriever(documentService.getContentRetriever())
                                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                                .build();

                        assistant.chat(chatId, userQuery)
                                .onPartialResponse(token -> sink.tryEmitNext(new TokenContent(token)))
                                .onCompleteResponse(response -> {
                                    sink.tryEmitNext(new TokenCompleteResponse("done"));
                                    sink.tryEmitComplete();
                                })
                                .onError(err -> {
                                    // Surface errors back to sink; they will trigger failover handling below
                                    sink.tryEmitError(err);
                                })
                                .start();

                        // If start didn't throw, we assume streaming has been initiated and callbacks
                        // will drive the sink. Return to stop trying other providers.
                        return;
                    } catch (Exception e) {
                        lastException = e;
                        // If we have more attempts for this provider, backoff then retry
                        if (attempt < maxRetries) {
                            try {
                                long sleepMs = backoffBaseMs * (1L << attempt);
                                Thread.sleep(sleepMs);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                sink.tryEmitError(ie);
                                return;
                            }
                        }
                        // otherwise continue to next attempt or next provider
                    }
                }
            }

            // If we reach here, all providers failed
            if (lastException != null) {
                sink.tryEmitError(new RuntimeException("All streaming model providers failed", lastException));
            } else {
                sink.tryEmitError(new RuntimeException("No model providers configured or available"));
            }
        });

        return sink.asFlux();
    }
}
