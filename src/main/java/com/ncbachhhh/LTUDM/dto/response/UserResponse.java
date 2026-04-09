package com.ncbachhhh.LTUDM.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ncbachhhh.LTUDM.entity.User.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String email;
    String username;

    @JsonProperty("display_name")
    String displayName;

    @JsonProperty("avatar_url")
    String avatarUrl;

    @JsonProperty("created_at")
    LocalDateTime createdAt;

    UserRole role;

    @JsonProperty("is_active")
    boolean active;
}
