-- ============================================================
-- SIFT V10 - Tweet image and video-thumbnail previews
-- ============================================================

CREATE TABLE tweet_media
(
    id          UUID PRIMARY KEY,
    tweet_id    UUID        NOT NULL,
    media_type  VARCHAR(20) NOT NULL,
    preview_url TEXT        NOT NULL,
    position    INTEGER     NOT NULL,

    CONSTRAINT fk_tweet_media_tweet
        FOREIGN KEY (tweet_id)
            REFERENCES tweets (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_tweet_media_tweet_position
        UNIQUE (tweet_id, position)
);

CREATE INDEX idx_tweet_media_tweet_id ON tweet_media (tweet_id);
