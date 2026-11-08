--liquibase formatted sql

--changeset course:009
CREATE INDEX api_keys_user_id_idx ON api_keys (user_id);

