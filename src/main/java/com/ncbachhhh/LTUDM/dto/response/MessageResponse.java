package com.ncbachhhh.LTUDM.dto.response;

import com.ncbachhhh.LTUDM.entity.Message.MessageType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private String id;                  // ID tin nhắn
    private String conversation_id;     // ID cuộc trò chuyện
    private String sender_id;           // ID người gửi
    private MessageType type;           // Loại tin nhắn
    private String content;             // Nội dung tin nhắn
    private LocalDateTime created_at;   // Thời gian gửi
    private boolean is_read;            // Đã đọc chưa
}
