package com.ncbachhhh.LTUDM.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.ncbachhhh.LTUDM.entity.User.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserRegisterRequest {
    @Email(message = "INVALID_EMAIL_FORMAT")
    @NotBlank(message = "INVALID_EMAIL_FORMAT")
    String email;

    @Size(min = 3, max = 50, message = "INVALID_USERNAME_FORMAT")
    String username;

    @NotBlank(message = "INVALID_PASSWORD_FORMAT")
    @Size(min = 8, message = "INVALID_PASSWORD_FORMAT")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
            message = "INVALID_PASSWORD_FORMAT"
    )
    String password;

    @JsonAlias({"display_name", "displayName"})
    @NotBlank(message = "INVALID_DISPLAY_NAME_FORMAT")
    @Size(max = 100, message = "INVALID_DISPLAY_NAME_FORMAT")
    String displayName;

    @Size(max = 20, message = "INVALID_GENDER_FORMAT")
    String gender;

    @JsonAlias({"birth_date", "birthDate", "dob"})
    @PastOrPresent(message = "INVALID_DOB")
    LocalDate dob;

    UserRole role;
}
