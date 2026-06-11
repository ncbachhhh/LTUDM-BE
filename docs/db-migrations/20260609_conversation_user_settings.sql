ALTER TABLE users
    ADD COLUMN notification_sound VARCHAR(100) NOT NULL DEFAULT 'default';

ALTER TABLE conversations
    ADD COLUMN emoji VARCHAR(20) NOT NULL DEFAULT '👍';

ALTER TABLE conversation_members
    ADD COLUMN muted_until DATETIME NULL;
