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
    @URL(message = "INVALID_AVATAR_URL")
    @Size(max = 500, message = "AVATAR_URL_TOO_LONG")
    String avatarUrl;
}
