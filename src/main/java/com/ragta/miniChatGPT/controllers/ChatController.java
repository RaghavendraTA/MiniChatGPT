package com.ragta.miniChatGPT.controllers;

import com.ragta.miniChatGPT.dtos.TokenResponse;
import com.ragta.miniChatGPT.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<TokenResponse> streamChat(@RequestParam int chatId, @RequestParam String userQuery) {
        return chatService.chat(chatId, userQuery);
    }
}
