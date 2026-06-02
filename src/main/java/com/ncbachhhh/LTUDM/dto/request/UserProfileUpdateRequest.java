package com.ncbachhhh.LTUDM.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class UserProfileUpdateRequest {
    @JsonAlias({"display_name", "displayName"})
    @Size(min = 1, max = 100, message = "INVALID_DISPLAY_NAME_FORMAT")
    String name;

    @Size(max = 20, message = "INVALID_GENDER_FORMAT")
    String gender;

    @PastOrPresent(message = "INVALID_DOB")
    LocalDate dob;

    @Pattern(regexp = "^[0-9+()\\-\\s]{0,20}$", message = "INVALID_PHONE_FORMAT")
    String phone;

    @Size(max = 50)
    String nickname;

    @Size(max = 500)
    String bio;

    @JsonProperty("background_url")
    @JsonAlias({"background_url", "backgroundUrl"})
    @Size(max = 500)
    String backgroundUrl;
}
