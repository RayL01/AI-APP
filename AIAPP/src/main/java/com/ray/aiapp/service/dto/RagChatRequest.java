package com.ray.aiapp.service.dto;

import jakarta.validation.constraints.NotBlank;

public record RagChatRequest(
        @NotBlank String sessionId,
        @NotBlank String message
) {}