package com.ray.aiapp.service.dto;

import java.time.Instant;
import java.util.UUID;

public record AssistantProfileResponse(
        UUID id,
        String name,
        String description,
        String model,
        double temperature,
        Instant createdAt,
        Instant updatedAt) {
}
