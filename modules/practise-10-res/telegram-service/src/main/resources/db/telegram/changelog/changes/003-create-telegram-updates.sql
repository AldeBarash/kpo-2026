--liquibase formatted sql

--changeset telegram:020
CREATE TABLE telegram_updates (
    update_id BIGINT PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
