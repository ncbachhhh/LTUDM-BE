package com.ncbachhhh.LTUDM.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserProfileResponse {
    String id;
    String email;
    String username;

    @JsonProperty("display_name")
    String displayName;

    @JsonProperty("avatar_url")
    String avatarUrl;

    @JsonProperty("background_url")
    String backgroundUrl;

    @JsonProperty("friendship_status")
    String friendshipStatus;

    @JsonProperty("friendship_direction")
    String friendshipDirection;

    @JsonProperty("is_online")
    boolean online;
}
