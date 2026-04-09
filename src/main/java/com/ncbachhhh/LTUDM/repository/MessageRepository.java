package com.ncbachhhh.LTUDM.repository;

import com.ncbachhhh.LTUDM.entity.Message.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    @Query("""
            SELECT m
            FROM Message m
            WHERE m.conversationId = :conversationId
              AND NOT EXISTS (
                  SELECT 1
                  FROM MessageDeletion md
                  WHERE md.id.messageId = m.id
                    AND md.id.userId = :userId
              )
            ORDER BY m.createdAt ASC
            """)
    List<Message> findVisibleMessagesByConversation(@Param("conversationId") String conversationId,
                                                    @Param("userId") String userId);

    @Query("""
            SELECT m
            FROM Message m
            WHERE m.conversationId = :conversationId
              AND NOT EXISTS (
                  SELECT 1
                  FROM MessageDeletion md
                  WHERE md.id.messageId = m.id
                    AND md.id.userId = :userId
              )
            ORDER BY m.createdAt DESC
            """)
    Page<Message> findVisibleMessagesByConversationPaged(@Param("conversationId") String conversationId,
                                                         @Param("userId") String userId,
                                                         Pageable pageable);

    @Query("""
            SELECT COUNT(m)
            FROM Message m
            WHERE m.conversationId = :conversationId
              AND m.senderId <> :userId
              AND NOT EXISTS (
                  SELECT 1
                  FROM MessageReceipt mr
                  WHERE mr.id.messageId = m.id
                    AND mr.id.userId = :userId
              )
              AND NOT EXISTS (
                  SELECT 1
                  FROM MessageDeletion md
                  WHERE md.id.messageId = m.id
                    AND md.id.userId = :userId
              )
            """)
    long countUnreadMessages(@Param("conversationId") String conversationId, @Param("userId") String userId);
}
