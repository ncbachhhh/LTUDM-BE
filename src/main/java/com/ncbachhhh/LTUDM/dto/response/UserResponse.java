package com.ncbachhhh.LTUDM.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ncbachhhh.LTUDM.entity.User.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
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

    @JsonProperty("background_url")
    String backgroundUrl;

    String gender;
    LocalDate dob;
    String phone;
    String nickname;
    String bio;

    @JsonProperty("created_at")
    LocalDateTime createdAt;

    UserRole role;

    @JsonProperty("is_active")
    boolean active;

    String showBirthday;
    boolean onlineStatus;
    boolean showEmail;
    boolean mentionSuggestions;
    boolean readReceipts;
    boolean notificationEnabled;

    @JsonProperty("sound_enabled")
    boolean soundEnabled;

    @JsonProperty("notification_sound")
    String notificationSound;

    @JsonProperty("theme_mode")
    String themeMode;

    @JsonProperty("chat_color")
    String chatColor;
}
