package com.ncbachhhh.LTUDM.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserLoginRequest {
    @Email(message = "INVALID_EMAIL_FORMAT")
    @NotNull
    String email;

    @Size(min = 8, message = "INVALID_PASSWORD_FORMAT")
    String password;
}
