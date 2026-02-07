package com.ncbachhhh.LTUDM.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
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
public class UserRegisterRequest {
    @Email(message = "INVALID_EMAIL_FORMAT")
    @NotNull
    String email;

    @Size(min = 3, max = 20, message = "INVALID_USERNAME_FORMAT")
    String username;

    @Size(min = 8, message = "INVALID_PASSWORD_FORMAT")
    String password;

    @Size(max = 30, message = "INVALID_DISPLAY_NAME_FORMAT")
    String display_name;
    String avatar_url;
}
