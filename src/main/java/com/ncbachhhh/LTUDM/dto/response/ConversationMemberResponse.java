package com.ncbachhhh.LTUDM.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ncbachhhh.LTUDM.entity.ConversationMembers.ConversationMemberRole;
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
public class ConversationMemberResponse {
    @JsonProperty("user_id")
    String userId;

    String username;

    @JsonProperty("display_name")
    String displayName;

    @JsonProperty("avatar_url")
    String avatarUrl;

    ConversationMemberRole role;

    @JsonProperty("joined_at")
    LocalDateTime joinedAt;
}
