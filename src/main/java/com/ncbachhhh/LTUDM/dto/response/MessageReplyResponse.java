package com.ncbachhhh.LTUDM.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ncbachhhh.LTUDM.entity.Message.MessageType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageReplyResponse {
    private String id;

    @JsonProperty("sender_id")
    private String senderId;

    private MessageType type;
    private String content;

    @JsonProperty("is_recalled")
    private boolean recalled;
}
