--liquibase formatted sql

--changeset course:017
CREATE UNIQUE INDEX outbox_business_key ON outbox_events(event_type, ai_request_id);
