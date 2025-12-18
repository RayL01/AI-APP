package com.ray.aiapp.config;

import com.ray.aiapp.config.properties.LangchainModelProperties;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(LangchainModelProperties.class)
public class LangchainConfiguration {

    private final LangchainModelProperties properties;

    @Bean
    @ConditionalOnProperty(prefix = "aiapp.langchain", name = "enabled", havingValue = "true", matchIfMissing = true)
    public OpenAiChatModel langchainOpenAiChatModel() {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            log.warn("aiapp.langchain.api-key is empty. LangChain4j model will not be able to reach OpenAI until it is provided.");
        }
        return OpenAiChatModel.builder()
                .apiKey(properties.getApiKey())
                .modelName(properties.getModel())
                .temperature(properties.getTemperature())
                .maxTokens(properties.getMaxTokens())
                .build();
    }
}
