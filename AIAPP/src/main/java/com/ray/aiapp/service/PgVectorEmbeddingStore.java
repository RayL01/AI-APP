package com.ray.aiapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ray.aiapp.domain.model.DocumentChunk;
import com.ray.aiapp.repository.DocumentChunkRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Custom EmbeddingStore implementation using PostgreSQL with pgvector extension.
 * This replaces the in-memory store with persistent vector storage.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PgVectorEmbeddingStore implements EmbeddingStore<TextSegment> {

    private final DocumentChunkRepository chunkRepository;
    private final ChunkService chunkService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public String add(Embedding embedding) {
        String embeddingId = UUID.randomUUID().toString();
        add(embeddingId, embedding);
        return embeddingId;
    }

    @Override
    @Transactional
    public void add(String embeddingId, Embedding embedding) {
        // Create a chunk without text segment (edge case, normally shouldn't happen)
        DocumentChunk chunk = DocumentChunk.builder()
                .embeddingId(embeddingId)
                .documentId("unknown")
                .text("")
                .embedding(embedding.vector())
                .metadata("{}")
                .chunkIndex(0)  // Default to 0 for edge case
                .build();

        chunkRepository.save(chunk);
        log.debug("Added embedding {} to vector store", embeddingId);
    }

    @Override
    @Transactional
    public String add(Embedding embedding, TextSegment textSegment) {
        String embeddingId = UUID.randomUUID().toString();

        // Extract metadata
        String documentId = textSegment.metadata().getString("documentId");
        if (documentId == null) {
            documentId = "unknown";
        }

        // chunkIndex must be provided by caller via metadata
        Integer chunkIndex = textSegment.metadata().getInteger("chunkIndex");
        if (chunkIndex == null) {
            chunkIndex = 0;
            log.warn("chunkIndex not provided in metadata, defaulting to 0");
        }

        String metadataJson;
        try {
            metadataJson = objectMapper.writeValueAsString(textSegment.metadata().toMap());
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize metadata: {}", e.getMessage());
            metadataJson = "{}";
        }

        DocumentChunk chunk = DocumentChunk.builder()
                .embeddingId(embeddingId)
                .documentId(documentId)
                .text(textSegment.text())
                .embedding(embedding.vector())
                .metadata(metadataJson)
                .chunkIndex(chunkIndex)
                .build();

        chunkRepository.save(chunk);
        log.debug("Added text segment {} to vector store for document {}", embeddingId, documentId);

        return embeddingId;
    }

    @Override
    @Transactional
    public List<String> addAll(List<Embedding> embeddings) {
        return embeddings.stream()
                .map(this::add)
                .toList();
    }

    @Override
    @Transactional
    public List<String> addAll(List<Embedding> embeddings, List<TextSegment> textSegments) {
        if (embeddings.size() != textSegments.size()) {
            throw new IllegalArgumentException("Number of embeddings and text segments must be equal");
        }

        List<String> embeddingIds = new ArrayList<>();
        for (int i = 0; i < embeddings.size(); i++) {
            String id = add(embeddings.get(i), textSegments.get(i));
            embeddingIds.add(id);
        }
        return embeddingIds;
    }


    @Override
    @Transactional(readOnly = true)
    public EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest request) {

        Embedding queryEmbedding = request.queryEmbedding();
        int maxResults = request.maxResults();
        double minScore = request.minScore();

        log.info("Searching for {} similar chunks with minScore {}", maxResults, minScore);

        // Delegate to ChunkService which handles:
        // 1. Vector string conversion
        // 2. Repository query (Interface Projection)
        // 3. String embedding -> float[] conversion
        // 4. EmbeddingMatch construction
        List<EmbeddingMatch<TextSegment>> matches = chunkService.searchSimilarChunks(
                queryEmbedding.vector(), maxResults, minScore);

        return new EmbeddingSearchResult<>(matches);
    }

    /**
     * Delete all chunks belonging to a document.
     */
    @Transactional
    public void deleteByDocumentId(String documentId) {
        chunkRepository.deleteByDocumentId(documentId);
        log.info("Deleted all chunks for document {}", documentId);
    }
}
