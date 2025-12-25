package com.ray.aiapp.controller;

import com.ray.aiapp.service.SearchChatService;
import com.ray.aiapp.service.dto.ChatRequest;
import com.ray.aiapp.service.dto.ChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for AI chat with web search capability.
 * The AI can autonomously search the internet when it needs current information.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search-chat")
@ConditionalOnBean(SearchChatService.class)
public class SearchChatController {

    private final SearchChatService searchChatService;

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        String response = searchChatService.chat(request.sessionId(), request.message());
        return new ChatResponse(request.sessionId(), response);
    }

    @DeleteMapping("/{sessionId}")
    public void clearMemory(@PathVariable String sessionId) {
        searchChatService.clearMemory(sessionId);
    }
}
