package com.ray.aiapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ray.aiapp.domain.converter.VectorAttributeConverter;
import com.ray.aiapp.repository.DocumentChunkRepository;
import com.ray.aiapp.service.dto.chatWithScore;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer for document chunk operations.
 * Encapsulates similarity search and converts Interface Projection results
 * to LangChain4j EmbeddingMatch objects.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkService {

    private final DocumentChunkRepository chunkRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Search for similar chunks and return as EmbeddingMatch list.
     * Handles the conversion from Interface Projection (String embedding) to float[].
     *
     * @param queryVector the query embedding vector
     * @param maxResults maximum number of results to return
     * @param minScore minimum similarity score threshold
     * @return list of EmbeddingMatch objects ready for LangChain4j
     */
    @Transactional(readOnly = true)
    public List<EmbeddingMatch<TextSegment>> searchSimilarChunks(
            float[] queryVector, int maxResults, double minScore) {

        String vectorString = VectorAttributeConverter.toVectorString(queryVector);

        log.debug("Searching for {} similar chunks with minScore {}", maxResults, minScore);

        List<chatWithScore> results = chunkRepository.findSimilarChunks(
                vectorString, maxResults, minScore);

        log.debug("Found {} matching chunks", results.size());

        return results.stream()
                .map(this::toEmbeddingMatch)
                .toList();
    }

    /**
     * Convert Interface Projection result to LangChain4j EmbeddingMatch.
     * Key transformation: String embedding -> float[] via VectorAttributeConverter.parseVector()
     */
    private EmbeddingMatch<TextSegment> toEmbeddingMatch(chatWithScore chunk) {
        // Convert String embedding to float[] using static method
        float[] embeddingVector = VectorAttributeConverter.parseVector(chunk.getEmbedding());

        // Parse metadata JSON to Map
        Map<String, Object> metadataMap = parseMetadata(chunk.getMetadata());

        return new EmbeddingMatch<>(
                chunk.getScore(),
                chunk.getEmbeddingId(),
                Embedding.from(embeddingVector),
                TextSegment.from(chunk.getText(), Metadata.from(metadataMap))
        );
    }

    /**
     * Parse metadata JSON string to Map.
     */
    private Map<String, Object> parseMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(metadataJson, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse metadata JSON: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
