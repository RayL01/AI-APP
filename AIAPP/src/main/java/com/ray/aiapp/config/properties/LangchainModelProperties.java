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
    private Integer maxTokens;

    private Rag rag = new Rag();
    private WebSearch webSearch = new WebSearch();

    @Getter
    @Setter
    public static class Rag {
        private boolean enabled = true;
        private String embeddingModel = "text-embedding-3-small";
        private int chunkSize = 500;
        private int chunkOverlap = 50;
        private int maxResults = 5;
        private double minScore = 0.5;
    }

    @Getter
    @Setter
    public static class WebSearch {
        private boolean enabled = false;
        private String apiKey;
        private int maxResults = 5;
    }
}
