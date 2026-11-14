--liquibase formatted sql

--changeset course:013 context:demo
INSERT INTO models (id, name, status, created_at)
VALUES ('76de0ba9-d229-46e9-91ac-74664c41d78d', 'qwen2.5:0.5b', 'ACTIVE', CURRENT_TIMESTAMP);

INSERT INTO model_prices (
    id,
    model_id,
    input_price_per_1000,
    output_price_per_1000,
    valid_from
)
VALUES (
    '88630294-3671-4416-bc12-a8bd7c73b457',
    '76de0ba9-d229-46e9-91ac-74664c41d78d',
    100,
    200,
    CURRENT_TIMESTAMP
);
