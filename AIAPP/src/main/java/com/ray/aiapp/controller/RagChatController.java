package com.ray.aiapp.controller;

import com.ray.aiapp.service.RagChatService;
import com.ray.aiapp.service.dto.RagChatRequest;
import com.ray.aiapp.service.dto.RagChatResponse;
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
@RequestMapping("/api/v1/rag-chat")
public class RagChatController {

    private final RagChatService ragChatService;

    @PostMapping
    public RagChatResponse chat(@Valid @RequestBody RagChatRequest request) {
        return ragChatService.chat(request.sessionId(), request.message());
    }

    @DeleteMapping("/{sessionId}")
    public void clearMemory(@PathVariable String sessionId) {
        ragChatService.clearMemory(sessionId);
    }
}