package com.ncbachhhh.LTUDM.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @JsonProperty("display_name")
    @Size(min = 1, max = 100, message = "INVALID_DISPLAY_NAME_FORMAT")
    String displayName;

    @JsonProperty("avatar_url")
    @URL(message = "Avatar URL must be a valid URL")
    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    String avatarUrl;
}
