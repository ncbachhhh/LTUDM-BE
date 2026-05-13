package com.ncbachhhh.LTUDM.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class VerifyResetOtpRequest {
    @NotBlank(message = "INVALID_EMAIL_FORMAT")
    @Email(message = "INVALID_EMAIL_FORMAT")
    String email;

    @NotBlank(message = "INVALID_RESET_OTP")
    @Pattern(regexp = "^\\d{6}$", message = "INVALID_RESET_OTP")
    String otp;
}
