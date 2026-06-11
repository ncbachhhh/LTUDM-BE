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
public class MessageSeenByResponse {
    @JsonProperty("user_id")
    String userId;

    String username;

    @JsonProperty("display_name")
    String displayName;

    String nickname;

    @JsonProperty("avatar_url")
    String avatarUrl;

    @JsonProperty("seen_at")
    LocalDateTime seenAt;
}
