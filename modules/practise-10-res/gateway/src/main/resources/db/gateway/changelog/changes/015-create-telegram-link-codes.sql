--liquibase formatted sql

--changeset course:015
CREATE TABLE telegram_link_codes (
    code_hash VARCHAR(64) PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    expires_at TIMESTAMPTZ NOT NULL
);
