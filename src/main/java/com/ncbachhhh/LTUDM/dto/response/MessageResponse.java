package com.ncbachhhh.LTUDM.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ncbachhhh.LTUDM.entity.Message.MessageType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MessageResponse {
    private String id;

    @JsonProperty("conversation_id")
    private String conversationId;

    @JsonProperty("sender_id")
    private String senderId;

    private MessageType type;
    private String content;
    private AttachmentResponse attachment;

    @JsonProperty("reply_to_message_id")
    private String replyToMessageId;

    @JsonProperty("reply_to_message")
    private MessageReplyResponse replyToMessage;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("is_read")
    private boolean read;

    @JsonProperty("seen_by")
    private List<MessageSeenByResponse> seenBy;

    @JsonProperty("is_pinned")
    private boolean pinned;

    @JsonProperty("pinned_by")
    private String pinnedBy;

    @JsonProperty("pinned_at")
    private LocalDateTime pinnedAt;

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
