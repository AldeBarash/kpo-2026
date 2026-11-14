--liquibase formatted sql

--changeset telegram:021
CREATE TABLE telegram_notifications (
    id UUID PRIMARY KEY,
    event_id UUID UNIQUE,
    update_id BIGINT UNIQUE REFERENCES telegram_updates(update_id),
    user_id UUID,
    chat_id BIGINT NOT NULL,
    binding_id UUID,
    payload JSONB NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'NEW' CHECK (status IN ('NEW','PROCESSING','SENT','FAILED','SKIPPED')),
    attempts INTEGER NOT NULL DEFAULT 0 CHECK (attempts >= 0),
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    locked_until TIMESTAMPTZ,
    lock_token UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    sent_at TIMESTAMPTZ,
    last_error TEXT,
    CHECK ((status = 'PROCESSING') = (lock_token IS NOT NULL AND locked_until IS NOT NULL)),
    CHECK ((event_id IS NULL) <> (update_id IS NULL)),
    CHECK (event_id IS NULL OR (user_id IS NOT NULL AND binding_id IS NOT NULL))
);

