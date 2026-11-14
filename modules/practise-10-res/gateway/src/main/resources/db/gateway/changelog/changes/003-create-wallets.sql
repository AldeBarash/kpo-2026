--liquibase formatted sql

--changeset course:003
CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users (id),
    balance BIGINT NOT NULL DEFAULT 0,
    reserved_balance BIGINT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT wallets_balance_check CHECK (balance >= 0),
    CONSTRAINT wallets_reserved_balance_check
        CHECK (reserved_balance >= 0 AND reserved_balance <= balance)
);

