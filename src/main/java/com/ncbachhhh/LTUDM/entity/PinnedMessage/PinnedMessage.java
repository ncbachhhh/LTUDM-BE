package com.ncbachhhh.LTUDM.entity.PinnedMessage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "pinned_messages")
public class PinnedMessage {
    @Id
    @Column(name = "message_id", length = 36)
    private String messageId;

    @Column(name = "conversation_id", nullable = false, length = 36)
    private String conversationId;

    @Column(name = "pinned_by", nullable = false, length = 36)
    private String pinnedBy;

    @Column(name = "pinned_at", nullable = false)
    private LocalDateTime pinnedAt;
}
