ALTER TABLE `messages`
  ADD COLUMN `reply_to_message_id` char(36) DEFAULT NULL AFTER `content`;

ALTER TABLE `messages`
  ADD KEY `reply_to_message_id` (`reply_to_message_id`);

ALTER TABLE `messages`
  ADD CONSTRAINT `messages_ibfk_4`
    FOREIGN KEY (`reply_to_message_id`) REFERENCES `messages` (`id`) ON DELETE SET NULL;
