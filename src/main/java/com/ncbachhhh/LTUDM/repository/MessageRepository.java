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

    // Lấy tin nhắn theo conversation_id, sắp xếp theo thời gian
    @Query("SELECT m FROM Message m WHERE m.conversation_id = :conversationId AND m.is_deleted = false ORDER BY m.created_at ASC")
    List<Message> findByConversationId(@Param("conversationId") String conversationId);

    // Lấy tin nhắn theo conversation_id với phân trang
    @Query("SELECT m FROM Message m WHERE m.conversation_id = :conversationId AND m.is_deleted = false ORDER BY m.created_at DESC")
    Page<Message> findByConversationIdPaged(@Param("conversationId") String conversationId, Pageable pageable);

    // Đếm số tin nhắn chưa đọc trong conversation
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation_id = :conversationId AND m.sender_id != :userId AND m.is_read = false AND m.is_deleted = false")
    long countUnreadMessages(@Param("conversationId") String conversationId, @Param("userId") String userId);

    // Lấy tin nhắn mới nhất của conversation
    @Query("SELECT m FROM Message m WHERE m.conversation_id = :conversationId AND m.is_deleted = false ORDER BY m.created_at DESC LIMIT 1")
    Message findLatestMessage(@Param("conversationId") String conversationId);
}
