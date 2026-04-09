package com.ncbachhhh.LTUDM.entity.MessageDeletion;

import com.ncbachhhh.LTUDM.entity.Key.MessageDeletionId;
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
@Table(name = "message_deletions")
public class MessageDeletion {
    @EmbeddedId
    private MessageDeletionId id;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
