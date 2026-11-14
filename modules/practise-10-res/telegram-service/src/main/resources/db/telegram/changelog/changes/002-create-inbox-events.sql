--liquibase formatted sql

--changeset telegram:019
CREATE TABLE inbox_events (
    consumer_name VARCHAR(64) NOT NULL,
    event_id UUID NOT NULL,
    outcome VARCHAR(16) NOT NULL CHECK (outcome IN ('QUEUED','NO_BINDING')),
    received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY(consumer_name, event_id)
);
