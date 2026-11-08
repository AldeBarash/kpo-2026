--liquibase formatted sql

--changeset course:010
CREATE INDEX model_prices_lookup_idx
    ON model_prices (model_id, valid_from DESC);

