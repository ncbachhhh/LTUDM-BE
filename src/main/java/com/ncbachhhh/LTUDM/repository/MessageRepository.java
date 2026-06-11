package com.ncbachhhh.LTUDM.repository;

import com.ncbachhhh.LTUDM.entity.Message.Message;
import com.ncbachhhh.LTUDM.entity.Message.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByConversationId(String conversationId);

    long countByConversationIdAndType(String conversationId, MessageType type);

    long countByConversationIdAndTypeAndContentContainingIgnoreCase(
            String conversationId,
            MessageType type,
            String content
    );

    @Modifying
    void deleteByConversationId(String conversationId);

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
            SELECT m
            FROM Message m
            WHERE m.conversationId = :conversationId
              AND m.type = :type
              AND m.recalled = false
              AND NOT EXISTS (
                  SELECT 1
                  FROM MessageDeletion md
                  WHERE md.id.messageId = m.id
                    AND md.id.userId = :userId
              )
            ORDER BY m.createdAt DESC
            """)
    Page<Message> findVisibleMessagesByConversationAndTypePaged(@Param("conversationId") String conversationId,
                                                                @Param("userId") String userId,
                                                                @Param("type") MessageType type,
                                                                Pageable pageable);

    @Query("""
            SELECT m
            FROM Message m
            WHERE m.conversationId = :conversationId
              AND m.type = com.ncbachhhh.LTUDM.entity.Message.MessageType.TEXT
              AND m.recalled = false
              AND m.content IS NOT NULL
              AND LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
              AND NOT EXISTS (
                  SELECT 1
                  FROM MessageDeletion md
                  WHERE md.id.messageId = m.id
                    AND md.id.userId = :userId
              )
            ORDER BY m.createdAt DESC
            """)
    Page<Message> searchVisibleTextMessages(@Param("conversationId") String conversationId,
                                            @Param("userId") String userId,
                                            @Param("keyword") String keyword,
                                            Pageable pageable);

    @Query("""
            SELECT m
            FROM Message m
            WHERE m.conversationId = :conversationId
              AND m.type = com.ncbachhhh.LTUDM.entity.Message.MessageType.TEXT
              AND m.recalled = false
              AND m.content IS NOT NULL
              AND (
                  LOWER(m.content) LIKE '%http://%'
                  OR LOWER(m.content) LIKE '%https://%'
                  OR LOWER(m.content) LIKE '%www.%'
                  OR m.content LIKE '%.%'
              )
              AND NOT EXISTS (
                  SELECT 1
                  FROM MessageDeletion md
                  WHERE md.id.messageId = m.id
                    AND md.id.userId = :userId
              )
            ORDER BY m.createdAt DESC
            """)
    List<Message> findVisibleLinkCandidateMessages(@Param("conversationId") String conversationId,
                                                   @Param("userId") String userId);

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
