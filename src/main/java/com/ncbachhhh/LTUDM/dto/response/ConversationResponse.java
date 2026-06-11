package com.ncbachhhh.LTUDM.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ncbachhhh.LTUDM.entity.Conversation.ConversationType;
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
public class ConversationResponse {
    String id;

    ConversationType type;

    String title;

    @JsonProperty("created_by")
    String createdBy;

    @JsonProperty("created_at")
    LocalDateTime createdAt;

    @JsonProperty("avatar_url")
    String avatarUrl;

    String emoji;

    @JsonProperty("muted_until")
    LocalDateTime mutedUntil;

    @JsonProperty("latest_message")
    MessageResponse latestMessage;

    @JsonProperty("unread_count")
    long unreadCount;

    @JsonProperty("friendship_status")
    String friendshipStatus;

    @JsonProperty("friendship_direction")
    String friendshipDirection;

    @JsonProperty("blocked_by_current_user")
    boolean blockedByCurrentUser;

    @JsonProperty("current_user_blocked")
    boolean currentUserBlocked;

    List<ConversationMemberResponse> members;
}
