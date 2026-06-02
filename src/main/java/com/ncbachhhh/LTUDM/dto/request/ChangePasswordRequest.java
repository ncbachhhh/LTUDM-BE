package com.ncbachhhh.LTUDM.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
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
public class ChangePasswordRequest {
    @NotBlank(message = "OLD_PASSWORD_REQUIRED")
    @JsonAlias({"old_password", "currentPassword"})
    String currentPassword;

    @NotBlank(message = "NEW_PASSWORD_REQUIRED")
    @Size(min = 8, message = "INVALID_PASSWORD_FORMAT")
    @JsonAlias({"new_password", "newPassword"})
    String newPassword;

    @JsonAlias({"confirm_password", "confirmPassword"})
    String confirmPassword;
}
