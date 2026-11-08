--liquibase formatted sql

--changeset course:004
CREATE TABLE models (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT models_status_check CHECK (status IN ('ACTIVE', 'DISABLED'))
);

