 -- Create document_chunks table for storing document embeddings
CREATE TABLE document_chunks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    embedding_id VARCHAR(255) NOT NULL UNIQUE,
    document_id VARCHAR(255) NOT NULL,
    text TEXT NOT NULL,
    embedding vector(1536) NOT NULL,
    metadata TEXT,
    chunk_index INTEGER NOT NULL
);

-- Create indexes for efficient querying
CREATE INDEX idx_document_id ON document_chunks(document_id);
CREATE INDEX idx_embedding_id ON document_chunks(embedding_id);

-- Optional: Create HNSW index for fast vector similarity search
-- Uncomment for production use after analyzing your data distribution
-- CREATE INDEX idx_embedding_hnsw ON document_chunks
--     USING hnsw (embedding vector_cosine_ops)
--     WITH (m = 16, ef_construction = 64);

-- Alternative: IVFFlat index (faster to build, but requires training)
-- CREATE INDEX idx_embedding_ivfflat ON document_chunks
--     USING ivfflat (embedding vector_cosine_ops)
--     WITH (lists = 100);