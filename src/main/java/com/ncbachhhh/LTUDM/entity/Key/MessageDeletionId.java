package com.ncbachhhh.LTUDM.entity.Key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MessageDeletionId implements Serializable {
    @Column(name = "message_id", length = 36)
    private String messageId;

    @Column(name = "user_id", length = 36)
    private String userId;
}
