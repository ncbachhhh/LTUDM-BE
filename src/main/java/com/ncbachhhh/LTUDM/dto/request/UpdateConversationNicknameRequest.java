package com.ncbachhhh.LTUDM.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateConversationNicknameRequest {
    @JsonProperty("nickname")
    @Size(max = 100, message = "INVALID_NICKNAME_FORMAT")
    String nickname;
}
