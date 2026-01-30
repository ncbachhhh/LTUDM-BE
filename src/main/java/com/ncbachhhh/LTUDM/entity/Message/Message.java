package com.ncbachhhh.LTUDM.entity.Message;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter

@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String conversation_id; // ID cuộc trò chuyện mà tin nhắn thuộc về
    private String sender_id;       // ID người gửi tin nhắn
    @Enumerated(EnumType.STRING)
    private MessageType type;       // Loại tin nhắn: TEXT, IMAGE, VIDEO, FILE
    private String content;         // Nội dung tin nhắn (văn bản hoặc URL tới tệp)
    private LocalDate created_at;       // Dấu thời gian khi tin nhắn được gửi
}
