-- ============================================================
-- SIFT V8 - Quote-tweet and reply support
-- ============================================================

ALTER TABLE tweets
    ADD COLUMN quoted_tweet_id UUID;

ALTER TABLE tweets
    ADD CONSTRAINT fk_tweets_quoted_tweet
        FOREIGN KEY (quoted_tweet_id)
            REFERENCES tweets (id)
            ON DELETE SET NULL;

ALTER TABLE tweets
    ADD COLUMN is_reply BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE tweets
    ADD COLUMN reply_to_username VARCHAR(255);