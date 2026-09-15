package com.ragta.miniChatGPT.services;

import com.ragta.miniChatGPT.dtos.TokenCompleteResponse;
import com.ragta.miniChatGPT.dtos.TokenContent;
import com.ragta.miniChatGPT.dtos.TokenResponse;
import com.ragta.miniChatGPT.llmconfig.MemoryProvider;
import com.ragta.miniChatGPT.services.interfaces.DietitianAgent;
import com.ragta.miniChatGPT.services.interfaces.Orchestrator;
import com.ragta.miniChatGPT.services.interfaces.TestAssistant;
import com.ragta.miniChatGPT.llmconfig.LLMProviderFactory;
import com.ragta.miniChatGPT.llmconfig.IModelProvider;
import com.ragta.miniChatGPT.tools.DietManagerTools;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final LLMProviderFactory providerFactory;
    private final DocumentService documentService;
    private final MemoryProvider memoryProvider;
    private final List<String> providerOrder;
    private final int maxRetries;
    private final long backoffBaseMs;
    private final ThreadPoolExecutor executor;
    private final ConcurrentHashMap<String, CompletableFuture<Void>> activeModelList;

    List<ToolSpecification> toolSpecifications = ToolSpecifications.toolSpecificationsFrom(DietManagerTools.class);

    @Autowired
    public ChatService(LLMProviderFactory providerFactory,
               DocumentService documentService,
               MemoryProvider memoryProvider,
               @Value("${llm.providers:ollama}") String providersCsv,
               @Value("${llm.maxRetries:2}") int maxRetries,
               @Value("${llm.backoffMs:500}") long backoffBaseMs,
               @Value("${llm.maxConcurrency:2}") int maxConcurrency,
               @Value("${llm.queueSize:200}") int queueSize) {

        this.providerFactory = providerFactory;
        this.documentService = documentService;
        this.memoryProvider = memoryProvider;
        this.providerOrder = Arrays.stream(providersCsv.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(String::toLowerCase)
            .collect(Collectors.toList());

        this.maxRetries = Math.max(0, maxRetries);
        this.backoffBaseMs = Math.max(50, backoffBaseMs);
        this.activeModelList = new ConcurrentHashMap<>();

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

                CompletableFuture<Void> requestFuture = activeModelList.get(providerName);
                if (requestFuture != null && !requestFuture.isDone()) {
                    continue;
                }

                requestFuture = new CompletableFuture<>();
                activeModelList.put(providerName, requestFuture);

                for (int attempt = 0; attempt <= maxRetries; attempt++) {
                    try {
                        // TODO: Need a centralized memory provider as it creates a new instance everytime.
                        Orchestrator assistant = AiServices.builder(Orchestrator.class)
                                .streamingChatModel(modelProvider.provideStreamingChatModel())
                                .contentRetriever(documentService.getContentRetriever())
                                .chatMemoryProvider(memoryProvider::getMemoryProvider)
                                .tools(new DietManagerTools())
                                .build();

                        CompletableFuture<Void> finalRequestFuture = requestFuture;
                        assistant.chat(chatId, userQuery)
                                .onPartialResponse(token -> sink.tryEmitNext(new TokenContent(token)))
                                .onCompleteResponse(response -> {
                                    sink.tryEmitNext(new TokenContent("\n\n---" + providerName));
                                    sink.tryEmitNext(new TokenCompleteResponse("done"));
                                    sink.tryEmitComplete();
                                    finalRequestFuture.complete(null);
                                    activeModelList.remove(providerName);
                                })
                                .onError(err -> {
                                    // Surface errors back to sink; they will trigger failover handling below
                                    finalRequestFuture.completeExceptionally(err);
                                    activeModelList.remove(providerName);
                                    // sink.tryEmitError(err);
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
