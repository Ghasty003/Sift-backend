-- ============================================================
-- SIFT V7 - Password reset tokens
-- ============================================================

CREATE TABLE password_reset_tokens
(
    id         UUID PRIMARY KEY,
    user_id    UUID         NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    used_at    TIMESTAMPTZ,

    CONSTRAINT fk_password_reset_tokens_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_password_reset_tokens_hash
        UNIQUE (token_hash)
);

CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens (user_id);