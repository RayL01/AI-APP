package com.ray.aiapp.config;

import com.ray.aiapp.config.properties.LangchainModelProperties;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "aiapp.langchain.rag", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RagConfiguration {

    private final LangchainModelProperties properties;

    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("Creating OpenAI embedding model: {}", properties.getRag().getEmbeddingModel());
        return OpenAiEmbeddingModel.builder()
                .apiKey(properties.getApiKey())
                .modelName(properties.getRag().getEmbeddingModel())
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        log.info("Creating in-memory embedding store");
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore,
                                              EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(properties.getRag().getMaxResults())
                .minScore(properties.getRag().getMinScore())
                .build();
    }
}
