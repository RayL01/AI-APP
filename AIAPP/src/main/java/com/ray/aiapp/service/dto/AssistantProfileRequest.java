package com.ray.aiapp.service.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssistantProfileRequest(
        @NotBlank String name,
        String description,
        @NotBlank String model,
        @NotNull @DecimalMin("0.0") @DecimalMax("2.0") Double temperature) {
}
