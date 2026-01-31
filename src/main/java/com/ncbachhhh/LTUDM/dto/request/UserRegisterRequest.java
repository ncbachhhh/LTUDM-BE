package com.ncbachhhh.LTUDM.dto.request;

import com.ncbachhhh.LTUDM.exception.ErrorCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequest {
    @Email(message = "INVALID_EMAIL_FORMAT")
    @NotNull
    private String email;

    @Size(min = 3, max = 20, message = "INVALID_USERNAME_FORMAT")
    private String username;

    @Size(min = 8, message = "INVALID_PASSWORD_FORMAT")
    private String password;

    @Size(max = 30, message = "INVALID_DISPLAY_NAME_FORMAT")
    private String display_name;
    private String avatar_url;
}
