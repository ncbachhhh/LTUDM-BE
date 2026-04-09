package com.ncbachhhh.LTUDM.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ncbachhhh.LTUDM.entity.Message.MessageType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private String id;

    @JsonProperty("conversation_id")
    private String conversationId;

    @JsonProperty("sender_id")
    private String senderId;

    private MessageType type;
    private String content;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("is_read")
    private boolean read;

    @JsonProperty("is_edited")
    private boolean edited;

    @JsonProperty("edited_at")
    private LocalDateTime editedAt;

    @JsonProperty("is_recalled")
    private boolean recalled;

    @JsonProperty("recalled_at")
    private LocalDateTime recalledAt;

    @JsonProperty("recalled_by")
    private String recalledBy;
}
