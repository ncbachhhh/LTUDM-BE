package com.ncbachhhh.LTUDM.dto.request;

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
public class ResetPasswordRequest {
    @NotBlank(message = "INVALID_RESET_TOKEN")
    String resetToken;

    @NotBlank(message = "INVALID_PASSWORD_FORMAT")
    @Size(min = 8, message = "INVALID_PASSWORD_FORMAT")
    String newPassword;
}
