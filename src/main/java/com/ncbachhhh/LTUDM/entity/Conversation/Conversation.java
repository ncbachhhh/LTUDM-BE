package com.ncbachhhh.LTUDM.entity.Conversation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter

@Table(name = "conversations")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Enumerated(EnumType.STRING)
    private ConversationType type;     // Loại phòng chat: DIRECT hoặc GROUP
    private String title;               // Tiêu đề phòng chat
    private LocalDate created_at;       // Ngày tạo phòng chat
    private String created_by;          // ID người tạo phòng chat
    private String avatar_url;          // URL avatar của phòng chat
}
