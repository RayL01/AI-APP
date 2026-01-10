-- Create assistant_profiles table for storing AI assistant configurations
CREATE TABLE assistant_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1024),
    model VARCHAR(255) NOT NULL,
    temperature DOUBLE PRECISION NOT NULL
);
