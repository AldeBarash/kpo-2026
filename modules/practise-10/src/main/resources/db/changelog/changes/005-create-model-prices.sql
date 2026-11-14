--liquibase formatted sql

--changeset course:005
CREATE TABLE model_prices (
    id UUID PRIMARY KEY,
    model_id UUID NOT NULL REFERENCES models (id),
    input_price_per_1000 BIGINT NOT NULL,
    output_price_per_1000 BIGINT NOT NULL,
    valid_from TIMESTAMPTZ NOT NULL,
    valid_to TIMESTAMPTZ,
    CONSTRAINT model_prices_input_check CHECK (input_price_per_1000 >= 0),
    CONSTRAINT model_prices_output_check CHECK (output_price_per_1000 >= 0),
    CONSTRAINT model_prices_period_check CHECK (valid_to IS NULL OR valid_to > valid_from)
);

