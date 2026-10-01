-- ============================================================
-- SIFT V6 - Google Sign-In support
-- ============================================================

ALTER TABLE users
    ALTER COLUMN password_hash DROP NOT NULL;

ALTER TABLE users
    ADD COLUMN auth_provider VARCHAR(20) NOT NULL DEFAULT 'local';

ALTER TABLE users
    ADD COLUMN google_id VARCHAR(255);

ALTER TABLE users
    ADD CONSTRAINT uk_users_google_id UNIQUE (google_id);

CREATE INDEX idx_users_google_id ON users (google_id);