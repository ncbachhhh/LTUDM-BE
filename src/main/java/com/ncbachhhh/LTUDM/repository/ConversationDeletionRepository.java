package com.ncbachhhh.LTUDM.repository;

import com.ncbachhhh.LTUDM.entity.ConversationDeletion.ConversationDeletion;
import com.ncbachhhh.LTUDM.entity.Key.ConversationDeletionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationDeletionRepository extends JpaRepository<ConversationDeletion, ConversationDeletionId> {
    @Modifying
    void deleteByIdConversationId(String conversationId);
}
