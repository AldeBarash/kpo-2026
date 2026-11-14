--liquibase formatted sql

--changeset course:018
CREATE INDEX outbox_ready ON outbox_events(status, next_attempt_at, locked_until, created_at);
