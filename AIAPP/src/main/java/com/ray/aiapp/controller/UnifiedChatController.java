package com.ray.aiapp.controller;

import com.ray.aiapp.service.UnifiedChatService;
import com.ray.aiapp.service.dto.ChatRequest;
import com.ray.aiapp.service.dto.ChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Unified REST controller for AI chat with ALL capabilities:
 * - Basic conversational AI
 * - RAG (answers from uploaded documents)
 * - Web search (real-time information from the internet)
 *
 * The AI autonomously decides which capabilities to use based on your question!
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/unified-chat")
public class UnifiedChatController {

    private final UnifiedChatService unifiedChatService;

    /**
     * Chat with the AI using all available capabilities.
     *
     * The AI will automatically:
     * - Search your uploaded documents when you ask about them
     * - Search the web when you ask about current events, weather, news, etc.
     * - Use its base knowledge for general questions
     *
     * Example requests:
     * - "What does my uploaded budget document say about Q4?" → Uses RAG
     * - "What's the weather in Tokyo today?" → Uses web search
     * - "Explain quantum physics" → Uses base knowledge
     * - "What does my report say about AI, and what's the latest AI news?" → Uses BOTH RAG and search!
     */
    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        String response = unifiedChatService.chat(request.sessionId(), request.message());
        return new ChatResponse(request.sessionId(), response);
    }

    /**
     * Clear conversation history for a session.
     * Note: This only clears chat memory, not uploaded documents.
     */
    @DeleteMapping("/{sessionId}")
    public void clearMemory(@PathVariable String sessionId) {
        unifiedChatService.clearMemory(sessionId);
    }
}
