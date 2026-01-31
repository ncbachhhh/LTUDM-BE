package com.ncbachhhh.LTUDM.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {
    @Email(message = "INVALID_EMAIL_FORMAT")
    @NotNull
    private String email;

    @Size(min = 8, message = "INVALID_PASSWORD_FORMAT")
    private String password;
}
