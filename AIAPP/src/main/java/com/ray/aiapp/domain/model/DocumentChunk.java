package com.ray.aiapp.domain.model;

import com.ray.aiapp.domain.converter.VectorAttributeConverter;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a chunk of a document with its embedding vector for similarity search.
 * Schema is managed by Flyway migrations (see V2__create_document_chunks_table.sql).
 *
 * <p><b>关系模型：</b></p>
 * <ul>
 *   <li>一个 Document 可以被分割成多个 DocumentChunk（一对多关系）</li>
 *   <li>例如：一个 10 页的 PDF 可能被切分成 50 个 chunk，每个 chunk 约 500 字符</li>
 *   <li>使用 documentId 字符串引用父文档，而不是 JPA 的 @ManyToOne，这是向量数据库的常见做法</li>
 * </ul>
 *
 * <p><b>索引说明（在 migration 中定义）：</b></p>
 * <ul>
 *   <li>idx_document_id: 加速按文档查询所有chunks（例如：删除文档时需要删除所有相关chunks）</li>
 *   <li>idx_embedding_id: 加速按embedding ID查询（LangChain4j 需要通过 ID 查找chunk）</li>
 *   <li>idx_embedding_hnsw/ivfflat: pgvector 向量索引（生产环境启用，见 migration 注释）</li>
 * </ul>
 */
@Entity
@Table(name = "document_chunks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunk extends AbstractAuditableEntity {

    /**
     * Unique identifier for this chunk in the embedding store.
     * This corresponds to the embedding ID used by LangChain4j.
     */
    @Column(nullable = false, unique = true)
    private String embeddingId;

    /**
     * Reference to the parent document this chunk belongs to.
     * 关联到 Document.id (UUID converted to String)
     */
    @Column(name = "document_id", nullable = false)
    private String documentId;

    /**
     * The actual text content of this chunk.
     * JPA maps String to TEXT in PostgreSQL automatically when length exceeds VARCHAR limits.
     */
    @Column(nullable = false, length = 10485760) // 10MB max
    private String text;

    /**
     * The embedding vector for this chunk.
     * Uses VectorAttributeConverter to convert between float[] and PostgreSQL vector type.
     *
     * Database column type: vector(1536) - defined in Flyway migration V2
     * Dimension: 1536 for text-embedding-3-small (OpenAI)
     */
    @Convert(converter = VectorAttributeConverter.class)
    @Column(nullable = false)
    private float[] embedding;

    /**
     * Metadata associated with this chunk (e.g., fileName, page number, etc.).
     * Stored as JSON string. JPA maps String to TEXT for large content.
     */
    @Column(length = 10485760) // 10MB max
    private String metadata;

    /**
     * Chunk index within the parent document (for ordering).
     * 用于按原始顺序重建文档内容
     */
    @Column(nullable = false)
    private Integer chunkIndex;
}