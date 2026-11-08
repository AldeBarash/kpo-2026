--liquibase formatted sql

--changeset course:007
CREATE TABLE usage_records (
    id UUID PRIMARY KEY,
    ai_request_id UUID NOT NULL UNIQUE REFERENCES ai_requests (id),
    prompt_tokens INTEGER NOT NULL,
    completion_tokens INTEGER NOT NULL,
    total_tokens INTEGER NOT NULL,
    model_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT usage_records_prompt_check CHECK (prompt_tokens >= 0),
    CONSTRAINT usage_records_completion_check CHECK (completion_tokens >= 0),
    CONSTRAINT usage_records_total_check
        CHECK (total_tokens = prompt_tokens + completion_tokens)
);

