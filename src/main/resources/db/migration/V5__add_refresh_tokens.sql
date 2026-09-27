-- ============================================================
-- SIFT V5 - Refresh tokens (rotation + reuse detection, multi-device)
-- ============================================================

CREATE TABLE refresh_tokens
(
    id             UUID PRIMARY KEY,

    user_id        UUID         NOT NULL,
    family_id      UUID         NOT NULL,

    token_hash     VARCHAR(255) NOT NULL,

    expires_at     TIMESTAMPTZ  NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    revoked_at     TIMESTAMPTZ,
    replaced_by_id UUID,

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_refresh_tokens_replaced_by
        FOREIGN KEY (replaced_by_id)
            REFERENCES refresh_tokens (id)
            ON DELETE SET NULL,

    CONSTRAINT uk_refresh_tokens_hash
        UNIQUE (token_hash)
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_family_id ON refresh_tokens (family_id);
CREATE INDEX idx_refresh_tokens_active ON refresh_tokens (user_id, revoked_at);