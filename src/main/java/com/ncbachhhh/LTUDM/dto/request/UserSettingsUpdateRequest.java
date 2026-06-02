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
    Boolean soundEnabled;

    @JsonProperty("theme_mode")
    @Pattern(regexp = "^(light|dark)$", message = "INVALID_THEME_MODE")
    String themeMode;

    @JsonProperty("chat_color")
    @Size(max = 500, message = "INVALID_CHAT_COLOR")
    @Pattern(regexp = "^(?!.*(?i:url\\s*\\())[^;{}<>]+$", message = "INVALID_CHAT_COLOR")
    String chatColor;
}
