CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- SIFT V1 - Initial Database Schema
-- PostgreSQL
-- ============================================================


-- ============================================================
-- USERS
-- ============================================================

CREATE TABLE users
(
    id            UUID PRIMARY KEY,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_users_email UNIQUE (email)
);


-- ============================================================
-- TWEETS
-- ============================================================

CREATE TABLE tweets
(
    id                UUID PRIMARY KEY,

    tweet_id          VARCHAR(50) NOT NULL,
    url               TEXT        NOT NULL,

    author_username   VARCHAR(255),
    author_name       VARCHAR(255),
    author_avatar_url TEXT,

    text              TEXT,

    created_at        TIMESTAMPTZ,

    fetched_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_tweets_tweet_id UNIQUE (tweet_id),
    CONSTRAINT uk_tweets_url UNIQUE (url)
);


-- ============================================================
-- COLLECTIONS
-- ============================================================

CREATE TABLE collections
(
    id          UUID PRIMARY KEY,

    user_id     UUID         NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description TEXT,

    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_collections_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_collections_user_name
        UNIQUE (user_id, name)
);


-- ============================================================
-- BOOKMARKS
-- ============================================================

CREATE TABLE bookmarks
(
    id            UUID PRIMARY KEY,

    user_id       UUID NOT NULL,
    tweet_id      UUID NOT NULL,
    collection_id UUID NOT NULL,

    is_favorite   BOOLEAN     NOT NULL DEFAULT FALSE,
    is_read       BOOLEAN     NOT NULL DEFAULT FALSE,

    saved_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_bookmarks_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_bookmarks_tweet
        FOREIGN KEY (tweet_id)
            REFERENCES tweets (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_bookmarks_collection
        FOREIGN KEY (collection_id)
            REFERENCES collections (id)
            ON DELETE RESTRICT,

    CONSTRAINT uk_bookmarks_user_tweet
        UNIQUE (user_id, tweet_id)
);


-- ============================================================
-- TAGS
-- ============================================================

CREATE TABLE tags
(
    id         UUID PRIMARY KEY,

    user_id    UUID         NOT NULL,
    name       VARCHAR(100) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_tags_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_tags_user_name
        UNIQUE (user_id, name)
);


-- ============================================================
-- BOOKMARK_TAGS
-- ============================================================

CREATE TABLE bookmark_tags
(
    bookmark_id UUID NOT NULL,
    tag_id      UUID NOT NULL,

    PRIMARY KEY (bookmark_id, tag_id),

    CONSTRAINT fk_bookmark_tags_bookmark
        FOREIGN KEY (bookmark_id)
            REFERENCES bookmarks (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_bookmark_tags_tag
        FOREIGN KEY (tag_id)
            REFERENCES tags (id)
            ON DELETE CASCADE
);


-- ============================================================
-- BOOKMARK_NOTES
-- ============================================================

CREATE TABLE bookmark_notes
(
    id          UUID PRIMARY KEY,

    bookmark_id UUID NOT NULL,
    content     TEXT NOT NULL,

    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_bookmark_notes_bookmark
        FOREIGN KEY (bookmark_id)
            REFERENCES bookmarks (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_bookmark_notes_bookmark
        UNIQUE (bookmark_id)
);


-- ============================================================
-- API TOKENS
-- ============================================================

CREATE TABLE api_tokens
(
    id           UUID PRIMARY KEY,

    user_id      UUID         NOT NULL,

    type         VARCHAR(50)  NOT NULL,
    name         VARCHAR(100),

    token_hash   VARCHAR(255) NOT NULL,

    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    last_used_at TIMESTAMPTZ,
    revoked_at   TIMESTAMPTZ,

    CONSTRAINT fk_api_tokens_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_api_tokens_hash
        UNIQUE (token_hash)
);


-- ============================================================
-- INDEXES
-- ============================================================

-- ------------------------------------------------------------
-- Bookmarks
-- ------------------------------------------------------------

CREATE INDEX idx_bookmarks_user_id
    ON bookmarks (user_id);

CREATE INDEX idx_bookmarks_user_saved_at
    ON bookmarks (user_id, saved_at DESC);

CREATE INDEX idx_bookmarks_user_favorite
    ON bookmarks (user_id, is_favorite);

CREATE INDEX idx_bookmarks_user_read
    ON bookmarks (user_id, is_read);

CREATE INDEX idx_bookmarks_collection_id
    ON bookmarks (collection_id);


-- ------------------------------------------------------------
-- Collections
-- ------------------------------------------------------------

CREATE INDEX idx_collections_user_id
    ON collections (user_id);


-- ------------------------------------------------------------
-- Tags
-- ------------------------------------------------------------

CREATE INDEX idx_tags_user_id
    ON tags (user_id);


-- ------------------------------------------------------------
-- Bookmark ↔ Tags
-- ------------------------------------------------------------

CREATE INDEX idx_bookmark_tags_tag_id
    ON bookmark_tags (tag_id);


-- ------------------------------------------------------------
-- API Tokens
-- ------------------------------------------------------------

CREATE INDEX idx_api_tokens_user_id
    ON api_tokens (user_id);

CREATE INDEX idx_api_tokens_active
    ON api_tokens (user_id, revoked_at);