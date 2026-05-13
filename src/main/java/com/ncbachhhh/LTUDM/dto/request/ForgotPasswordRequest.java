package com.ncbachhhh.LTUDM.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class ForgotPasswordRequest {
    @NotBlank(message = "INVALID_EMAIL_FORMAT")
    @Email(message = "INVALID_EMAIL_FORMAT")
    String email;
}
