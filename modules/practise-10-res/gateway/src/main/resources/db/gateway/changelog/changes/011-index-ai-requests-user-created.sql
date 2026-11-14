--liquibase formatted sql

--changeset course:011
CREATE INDEX ai_requests_user_created_idx
    ON ai_requests (user_id, created_at DESC);

