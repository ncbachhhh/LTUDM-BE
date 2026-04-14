package com.ncbachhhh.LTUDM.repository;

import com.ncbachhhh.LTUDM.entity.Conversation.Conversation;
import com.ncbachhhh.LTUDM.entity.Conversation.ConversationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    List<Conversation> findByIdInAndType(Collection<String> ids, ConversationType type);
}
