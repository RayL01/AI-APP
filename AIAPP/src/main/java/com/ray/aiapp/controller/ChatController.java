package com.ray.aiapp.controller;

import com.ray.aiapp.service.ChatService;
import com.ray.aiapp.service.dto.ChatRequest;
import com.ray.aiapp.service.dto.ChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        String response = chatService.chat(request.sessionId(), request.message());
        return new ChatResponse(request.sessionId(), response);
    }

    @DeleteMapping("/{sessionId}")
    public void clearMemory(@PathVariable String sessionId) {
        chatService.clearMemory(sessionId);
    }
}