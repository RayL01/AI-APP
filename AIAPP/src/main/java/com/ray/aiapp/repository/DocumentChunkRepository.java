package com.ray.aiapp.repository;

import com.ray.aiapp.domain.model.DocumentChunk;
import com.ray.aiapp.service.dto.chatWithScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for DocumentChunk entities.
 * Provides CRUD operations and vector similarity search using pgvector.
 */
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

    /**
     * Find a chunk by its embedding ID.
     */
    Optional<DocumentChunk> findByEmbeddingId(String embeddingId);

    /**
     * Find all chunks belonging to a specific document.
     */
    List<DocumentChunk> findByDocumentIdOrderByChunkIndex(String documentId);

    /**
     * Delete all chunks belonging to a specific document.
     */
    void deleteByDocumentId(String documentId);

    /**
     * Count chunks for a specific document.
     */
    long countByDocumentId(String documentId);

    /**
     * Find similar chunks using cosine distance.
     * Uses pgvector's <=> operator for cosine distance.
     * Lower distance means more similar.
     *
     * @param queryEmbedding the embedding vector to search for
     * @param maxResults maximum number of results to return
     * @param minScore minimum similarity score (1 - cosine_distance)
     * @return list of chunks ordered by similarity (most similar first)
     */
    @Query(value = """
        SELECT
            embedding_id AS embeddingId,
            text,
            metadata,
            embedding::text AS embedding,
            (1 - (embedding <=> CAST(:queryEmbedding AS vector))) AS score
        FROM document_chunks
        WHERE (1 - (embedding <=> CAST(:queryEmbedding AS vector))) >= :minScore
        ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
        LIMIT :maxResults
        """, nativeQuery = true)
    List<chatWithScore> findSimilarChunks(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("maxResults") int maxResults,
            @Param("minScore") double minScore
    );

    /**
     * Find similar chunks within a specific document.
     */
    @Query(value = """
        SELECT * FROM document_chunks
        WHERE document_id = :documentId
        AND (1 - (embedding <=> CAST(:queryEmbedding AS vector))) >= :minScore
        ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
        LIMIT :maxResults
        """, nativeQuery = true)
    List<DocumentChunk> findSimilarChunksInDocument(
            @Param("documentId") String documentId,
            @Param("queryEmbedding") String queryEmbedding,
            @Param("maxResults") int maxResults,
            @Param("minScore") double minScore
    );
}
