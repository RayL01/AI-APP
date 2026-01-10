-- Create documents table for storing uploaded document metadata
CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    chunk_count INTEGER NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(50) NOT NULL
);

-- Create index for efficient querying by status
CREATE INDEX idx_documents_status ON documents(status);
