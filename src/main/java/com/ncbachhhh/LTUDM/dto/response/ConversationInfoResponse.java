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
public class ConversationInfoResponse {
    String id;

    ConversationType type;

    String title;

    @JsonProperty("display_name")
    String displayName;

    @JsonProperty("avatar_url")
    String avatarUrl;

    String emoji;

    @JsonProperty("muted_until")
    LocalDateTime mutedUntil;

    String status;

    @JsonProperty("created_by")
    String createdBy;

    @JsonProperty("created_at")
    LocalDateTime createdAt;

    @JsonProperty("member_count")
    long memberCount;

    List<ConversationMemberResponse> members;

    List<ConversationInfoStatResponse> stats;

    List<String> settings;
}
