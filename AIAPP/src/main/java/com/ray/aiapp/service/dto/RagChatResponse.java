package com.ray.aiapp.service.dto;

public record RagChatResponse(
        String sessionId,
        String message
) {}