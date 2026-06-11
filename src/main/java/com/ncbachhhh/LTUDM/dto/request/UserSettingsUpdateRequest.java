package com.ncbachhhh.LTUDM.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class UserSettingsUpdateRequest {
    @Pattern(regexp = "^(full|hide)$", message = "INVALID_SHOW_BIRTHDAY")
    String showBirthday;

    Boolean onlineStatus;
    Boolean showEmail;
    Boolean mentionSuggestions;
    Boolean readReceipts;
    Boolean notificationEnabled;

    @JsonProperty("sound_enabled")
    Boolean soundEnabled;

    @JsonProperty("notification_sound")
    @Pattern(regexp = "^(default|sound2|sound3|sound4|sound5|sound6|sound7|sound8)$", message = "INVALID_NOTIFICATION_SOUND")
    String notificationSound;

    @JsonProperty("theme_mode")
    @Pattern(regexp = "^(light|dark)$", message = "INVALID_THEME_MODE")
    String themeMode;

    @JsonProperty("chat_color")
    @Size(max = 500, message = "INVALID_CHAT_COLOR")
    @Pattern(regexp = "^(?!.*(?i:url\\s*\\())[^;{}<>]+$", message = "INVALID_CHAT_COLOR")
    String chatColor;
}
