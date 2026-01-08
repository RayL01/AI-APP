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

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Custom EmbeddingStore implementation using PostgreSQL with pgvector extension.
 * This replaces the in-memory store with persistent vector storage.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PgVectorEmbeddingStore implements EmbeddingStore<TextSegment> {

    private final DocumentChunkRepository chunkRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Thread-safe counter for generating embedding IDs within a document
    private final AtomicInteger chunkCounter = new AtomicInteger(0);

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
                .embedding(toFloatArray(embedding.vector()))
                .metadata("{}")
                .chunkIndex(chunkCounter.getAndIncrement())
                .build();

        chunkRepository.save(chunk);
        log.debug("Added embedding {} to vector store", embeddingId);
    }

    @Override
    @Transactional
    public String add(Embedding embedding, TextSegment textSegment) {
        String embeddingId = UUID.randomUUID().toString();
        add(embeddingId, embedding, textSegment);
        return embeddingId;
    }

    @Override
    @Transactional
    public void add(String embeddingId, Embedding embedding, TextSegment textSegment) {
        // Extract metadata
        String documentId = textSegment.metadata().getString("documentId", "unknown");
        int chunkIndex = textSegment.metadata().getInteger("chunkIndex", chunkCounter.getAndIncrement());

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
                .embedding(toFloatArray(embedding.vector()))
                .metadata(metadataJson)
                .chunkIndex(chunkIndex)
                .build();

        chunkRepository.save(chunk);
        log.debug("Added text segment {} to vector store for document {}", embeddingId, documentId);
    }

    @Override
    @Transactional
    public List<String> addAll(List<Embedding> embeddings) {
        return embeddings.stream()
                .map(this::add)
                .collect(Collectors.toList());
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

    // TODO(human): Implement the search method
    // This is the core method for vector similarity search
    @Override
    @Transactional(readOnly = true)
    public EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest request) {
        // TODO(human): Extract search parameters from request
        // - Get the query embedding
        // - Get maxResults (how many similar chunks to return)
        // - Get minScore (minimum similarity threshold)

        // TODO(human): Convert the embedding to a format suitable for the database query
        // Hint: You need to convert float[] to a PostgreSQL array string format like '[0.1,0.2,0.3]'

        // TODO(human): Call the repository method to find similar chunks

        // TODO(human): Convert DocumentChunk results to EmbeddingMatch<TextSegment>
        // For each chunk:
        //   1. Calculate the similarity score (1 - cosine_distance)
        //   2. Reconstruct the TextSegment from chunk.text and chunk.metadata
        //   3. Create an EmbeddingMatch with the score, embeddingId, embedding, and textSegment

        // TODO(human): Return EmbeddingSearchResult with the matches

        throw new UnsupportedOperationException("Search method not implemented yet");
    }

    /**
     * Delete all chunks belonging to a document.
     */
    @Transactional
    public void deleteByDocumentId(String documentId) {
        chunkRepository.deleteByDocumentId(documentId);
        log.info("Deleted all chunks for document {}", documentId);
    }

    /**
     * Convert double array to float array.
     */
    private float[] toFloatArray(float[] doubleArray) {
        return doubleArray;
    }

    /**
     * Convert float array to PostgreSQL vector string format.
     * Example: [0.1, 0.2, 0.3] -> "[0.1,0.2,0.3]"
     */
    private String toVectorString(float[] vector) {
        return "[" + Arrays.stream(vector)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(",")) + "]";
    }

    /**
     * Reconstruct metadata map from JSON string.
     */
    private Map<String, Object> parseMetadata(String metadataJson) {
        try {
            return objectMapper.readValue(metadataJson, Map.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse metadata JSON: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
