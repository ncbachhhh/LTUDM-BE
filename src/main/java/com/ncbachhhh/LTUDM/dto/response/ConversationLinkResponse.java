package com.ncbachhhh.LTUDM.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConversationLinkResponse {
    @JsonProperty("message_id")
    private String messageId;

    @JsonProperty("conversation_id")
    private String conversationId;

    @JsonProperty("sender_id")
    private String senderId;

    private String url;

    @JsonProperty("normalized_url")
    private String normalizedUrl;

    private String text;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
