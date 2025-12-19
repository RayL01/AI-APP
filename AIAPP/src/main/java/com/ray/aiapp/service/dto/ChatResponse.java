package com.ray.aiapp.service.dto;

public record ChatResponse(
        String sessionId,
        String message
) {}