package com.ncbachhhh.LTUDM.repository;

import com.ncbachhhh.LTUDM.entity.PinnedMessage.PinnedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PinnedMessageRepository extends JpaRepository<PinnedMessage, String> {
    long countByConversationId(String conversationId);

    List<PinnedMessage> findByConversationIdOrderByPinnedAtDesc(String conversationId);

    Optional<PinnedMessage> findByMessageId(String messageId);

    List<PinnedMessage> findByMessageIdIn(Collection<String> messageIds);
}
