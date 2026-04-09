package com.ncbachhhh.LTUDM.entity.Message;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "conversation_id", nullable = false, length = 36)
    private String conversationId;

    @Column(name = "sender_id", nullable = false, length = 36)
    private String senderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.TEXT;

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_edited", nullable = false)
    private boolean edited = false;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "is_recalled", nullable = false)
    private boolean recalled = false;

    @Column(name = "recalled_at")
    private LocalDateTime recalledAt;

    @Column(name = "recalled_by", length = 36)
    private String recalledBy;
}
