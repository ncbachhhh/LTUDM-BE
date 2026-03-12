package com.ncbachhhh.LTUDM.entity.Message;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;                      // ID tin nhắn
    private String conversation_id;         // ID cuộc trò chuyện mà tin nhắn thuộc về
    private String sender_id;               // ID người gửi tin nhắn
    @Enumerated(EnumType.STRING)
    private MessageType type = MessageType.TEXT;  // Loại tin nhắn: TEXT, IMAGE, VIDEO, FILE
    private String content;                 // Nội dung tin nhắn (văn bản hoặc URL tới tệp)
    private LocalDateTime created_at = LocalDateTime.now();  // Thời gian gửi tin nhắn
    private boolean is_read = false;        // Đã đọc chưa
    private boolean is_deleted = false;     // Đã xóa chưa (soft delete)
}
