package com.ncbachhhh.LTUDM.entity.ConversationDeletion;

import com.ncbachhhh.LTUDM.entity.Key.ConversationDeletionId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "conversation_deletions")
public class ConversationDeletion {
    @EmbeddedId
    private ConversationDeletionId id;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
