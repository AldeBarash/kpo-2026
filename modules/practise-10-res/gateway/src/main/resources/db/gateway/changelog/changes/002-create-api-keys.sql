--liquibase formatted sql

--changeset course:002
CREATE TABLE api_keys (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    key_prefix VARCHAR(32) NOT NULL,
    key_hash VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    last_used_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    CONSTRAINT api_keys_status_check CHECK (status IN ('ACTIVE', 'REVOKED'))
);

