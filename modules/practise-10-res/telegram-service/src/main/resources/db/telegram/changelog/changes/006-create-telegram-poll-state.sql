--liquibase formatted sql

--changeset telegram:023
CREATE TABLE telegram_poll_state (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    next_offset BIGINT NOT NULL DEFAULT 0,
    lock_token UUID,
    locked_until TIMESTAMPTZ
);
