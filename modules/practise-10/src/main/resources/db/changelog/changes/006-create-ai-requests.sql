--liquibase formatted sql

--changeset course:006
CREATE TABLE ai_requests (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    api_key_id UUID NOT NULL REFERENCES api_keys (id),
    model_id UUID NOT NULL REFERENCES models (id),
    model_price_id UUID NOT NULL REFERENCES model_prices (id),
    idempotency_key VARCHAR(255) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    max_output_tokens INTEGER NOT NULL,
    reserved_credits BIGINT NOT NULL,
    actual_cost BIGINT,
    response_content TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    error_code VARCHAR(128),
    CONSTRAINT ai_requests_idempotency_unique UNIQUE (user_id, idempotency_key),
    CONSTRAINT ai_requests_status_check
        CHECK (status IN ('RESERVED', 'PROCESSING', 'COMPLETED', 'FAILED', 'EXPIRED')),
    CONSTRAINT ai_requests_max_tokens_check CHECK (max_output_tokens > 0),
    CONSTRAINT ai_requests_reserved_check CHECK (reserved_credits >= 0),
    CONSTRAINT ai_requests_cost_check CHECK (actual_cost IS NULL OR actual_cost >= 0)
);

