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
public class ChangePasswordRequest {
    @NotBlank(message = "Old password is required")
    String old_password;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "INVALID_PASSWORD_FORMAT")
    String new_password;
}
