--liquibase formatted sql

--changeset course:008
CREATE TABLE wallet_transactions (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL REFERENCES wallets (id),
    ai_request_id UUID REFERENCES ai_requests (id),
    type VARCHAR(32) NOT NULL,
    amount BIGINT NOT NULL,
    balance_after BIGINT NOT NULL,
    reserved_after BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT wallet_transactions_type_check
        CHECK (type IN ('TOP_UP', 'RESERVE', 'CHARGE', 'RELEASE', 'ADJUSTMENT')),
    CONSTRAINT wallet_transactions_balance_check CHECK (balance_after >= 0),
    CONSTRAINT wallet_transactions_reserved_check
        CHECK (reserved_after >= 0 AND reserved_after <= balance_after)
);

