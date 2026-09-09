-- ============================================================
-- SIFT V4 - Add full_name to users
-- ============================================================

ALTER TABLE users
    ADD COLUMN full_name VARCHAR(255);

-- Backfill existing rows so the NOT NULL constraint below doesn't fail.
-- Uses the email's local part as a placeholder; adjust or skip this
-- UPDATE if you know the users table is currently empty.
UPDATE users
SET full_name = split_part(email, '@', 1)
WHERE full_name IS NULL;

ALTER TABLE users
    ALTER COLUMN full_name SET NOT NULL;