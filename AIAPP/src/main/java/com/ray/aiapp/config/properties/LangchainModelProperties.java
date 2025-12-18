package com.ray.aiapp.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "aiapp.langchain")
public class LangchainModelProperties {

    private boolean enabled = true;
    private String apiKey;
    private String model = "gpt-4o-mini";
    private double temperature = 0.2;
    private int maxTokens = 1024;
}
