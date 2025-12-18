package com.ray.aiapp.config;

import com.ray.aiapp.config.properties.AiClientProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AiClientProperties.class)
public class AiClientConfiguration {

    private final AiClientProperties properties;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        log.info("Initializing Spring AI ChatClient with model {}", properties.getDefaultModel());
        return builder
                .defaultSystem(properties.getSystemPrompt())
                .defaultOptions(OpenAiChatOptions.builder()
                        .withModel(properties.getDefaultModel())
                        .withTemperature((float) properties.getTemperature())
                        .withMaxTokens(properties.getMaxTokens())
                        .build())
                .build();
    }
}
