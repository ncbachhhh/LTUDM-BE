package com.ncbachhhh.LTUDM.dto.request;

import jakarta.validation.constraints.Pattern;
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
}
