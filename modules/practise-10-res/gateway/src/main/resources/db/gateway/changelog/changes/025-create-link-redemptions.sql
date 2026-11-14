--liquibase formatted sql

--changeset course:025
CREATE TABLE telegram_link_redemptions (
    update_id BIGINT PRIMARY KEY,
    code_hash VARCHAR(64) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id),
    telegram_user_id BIGINT NOT NULL,
    chat_id BIGINT NOT NULL,
    redeemed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
