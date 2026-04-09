package com.ncbachhhh.LTUDM.entity.MessageReceipt;

import com.ncbachhhh.LTUDM.entity.Key.MessageReceiptId;
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
@Table(name = "message_receipts")
public class MessageReceipt {
    @EmbeddedId
    private MessageReceiptId id;

    @Column(name = "seen_at")
    private LocalDateTime seenAt;
}
