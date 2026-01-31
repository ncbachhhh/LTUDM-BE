package com.ncbachhhh.LTUDM.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequest {
    @Email(message = "Invalid email format")
    @NotNull
    private String email;

    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Size(max = 30, message = "Display name must be at most 30 characters long")
    private String display_name;
    private String avatar_url;
}
