--liquibase formatted sql

--changeset course:016
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL REFERENCES users(id),
    aggregate_type VARCHAR(32) NOT NULL,
    ai_request_id UUID NOT NULL REFERENCES ai_requests(id),
    event_type VARCHAR(64) NOT NULL,
    schema_version INTEGER NOT NULL CHECK (schema_version > 0),
    payload JSONB NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'NEW' CHECK (status IN ('NEW','PROCESSING','SENT','FAILED','SKIPPED')),
    attempts INTEGER NOT NULL DEFAULT 0 CHECK (attempts >= 0),
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    locked_until TIMESTAMPTZ,
    lock_token UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    sent_at TIMESTAMPTZ,
    last_error TEXT,
    CHECK ((status = 'PROCESSING') = (lock_token IS NOT NULL AND locked_until IS NOT NULL))
);
