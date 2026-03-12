package com.ncbachhhh.LTUDM.dto.request;

import com.ncbachhhh.LTUDM.entity.Message.MessageType;
import lombok.Data;

@Data
public class MessageRequest {
    private String conversation_id;     // ID cuộc trò chuyện
    private String content;             // Nội dung tin nhắn
    private MessageType type;           // Loại tin nhắn: TEXT, IMAGE, VIDEO, FILE (mặc định TEXT)
}
