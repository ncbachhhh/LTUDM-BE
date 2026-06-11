package com.ncbachhhh.LTUDM.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ConversationRealtimeEventResponse {
    @JsonProperty("event_type")
    String eventType;

    @JsonProperty("conversation_id")
    String conversationId;

    @JsonProperty("actor_user_id")
    String actorUserId;

    @JsonProperty("target_user_id")
    String targetUserId;

    ConversationResponse conversation;

    @JsonProperty("occurred_at")
    LocalDateTime occurredAt;
}
