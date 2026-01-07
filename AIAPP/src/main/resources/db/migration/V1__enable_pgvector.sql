-- Enable pgvector extension for vector similarity search
CREATE EXTENSION IF NOT EXISTS vector;

-- Note: The document_chunks table will be created automatically by JPA/Hibernate
-- This migration only enables the pgvector extension