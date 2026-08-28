-- ============================================================
-- SIFT V2 - Allow Uncategorized Bookmarks
-- ============================================================

ALTER TABLE bookmarks
    ALTER COLUMN collection_id DROP NOT NULL;