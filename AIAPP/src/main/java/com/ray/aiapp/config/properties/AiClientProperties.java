package com.ray.aiapp.config.properties;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "aiapp.chat")
public class AiClientProperties {

    @NotBlank
    private String systemPrompt = "You are a helpful AI co-pilot for building enterprise apps.";

    @NotBlank
    private String defaultModel = "gpt-4o-mini";

    @DecimalMin("0.0")
    @DecimalMax("2.0")
    private double temperature = 0.2;

    @Min(128)
    @Max(4096)
    private int maxTokens = 1024;
}
