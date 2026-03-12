package com.ncbachhhh.LTUDM.repository;

import com.ncbachhhh.LTUDM.entity.Message.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    // Lấy tin nhắn theo conversation_id, sắp xếp theo thời gian
    List<Message> findByConversation_idAndIs_deletedFalseOrderByCreated_atAsc(String conversationId);

    // Lấy tin nhắn theo conversation_id với phân trang
    Page<Message> findByConversation_idAndIs_deletedFalseOrderByCreated_atDesc(String conversationId, Pageable pageable);

    // Đếm số tin nhắn chưa đọc trong conversation
    long countByConversation_idAndSender_idNotAndIs_readFalseAndIs_deletedFalse(String conversationId, String userId);

    // Lấy tin nhắn mới nhất của conversation
    Message findTopByConversation_idAndIs_deletedFalseOrderByCreated_atDesc(String conversationId);
}
