--liquibase formatted sql

--changeset course:012
CREATE INDEX wallet_transactions_wallet_created_idx
    ON wallet_transactions (wallet_id, created_at DESC);

