--liquibase formatted sql

--changeset telegram:014
CREATE TABLE telegram_bindings (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    telegram_user_id BIGINT NOT NULL UNIQUE,
    chat_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
