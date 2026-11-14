--liquibase formatted sql

--changeset telegram:024
INSERT INTO telegram_poll_state(id) VALUES (1);
