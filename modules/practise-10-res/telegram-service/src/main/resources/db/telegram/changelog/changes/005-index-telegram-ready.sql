--liquibase formatted sql

--changeset telegram:022
CREATE INDEX telegram_ready ON telegram_notifications(status, next_attempt_at, locked_until, created_at);
