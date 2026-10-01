-- ============================================================
-- SIFT V9 - Repost support
-- ============================================================

ALTER TABLE tweets
    ADD COLUMN reposted_by_name VARCHAR(255);

ALTER TABLE tweets
    ADD COLUMN reposted_by_username VARCHAR(255);