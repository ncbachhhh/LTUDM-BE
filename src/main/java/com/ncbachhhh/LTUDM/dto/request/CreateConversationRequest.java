package com.ncbachhhh.LTUDM.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CreateConversationRequest {
    @NotNull
    String type;

    String title;

    @JsonProperty("avatar_url")
    String avatarUrl;

    @JsonProperty("member_ids")
    List<String> memberIds;
}
