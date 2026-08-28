ALTER TABLE api_tokens
    ADD COLUMN token_id VARCHAR(100);

ALTER TABLE api_tokens
    ADD CONSTRAINT uk_api_tokens_token_id
        UNIQUE (token_id);

CREATE INDEX idx_api_tokens_token_id
    ON api_tokens (token_id);