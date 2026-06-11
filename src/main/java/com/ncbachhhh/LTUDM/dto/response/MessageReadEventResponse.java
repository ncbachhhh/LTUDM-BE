package com.ncbachhhh.LTUDM.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class MessageReadEventResponse {
    @JsonProperty("event_type")
    String eventType;

    @JsonProperty("conversation_id")
    String conversationId;

    MessageSeenByResponse reader;

    @JsonProperty("message_ids")
    List<String> messageIds;

    @JsonProperty("occurred_at")
    LocalDateTime occurredAt;
}
