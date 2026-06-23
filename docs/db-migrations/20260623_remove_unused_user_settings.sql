ALTER TABLE users
    DROP COLUMN IF EXISTS show_birthday,
    DROP COLUMN IF EXISTS online_status,
    DROP COLUMN IF EXISTS show_email,
    DROP COLUMN IF EXISTS mention_suggestions,
    DROP COLUMN IF EXISTS read_receipts,
    DROP COLUMN IF EXISTS notification_enabled;
