package com.ncbachhhh.LTUDM.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ncbachhhh.LTUDM.entity.Message.MessageType;
import lombok.Data;

@Data
public class MessageRequest {
    @JsonProperty("conversation_id")
    private String conversationId;

    private String content;

    private MessageType type;

    @JsonProperty("reply_to_message_id")
    @JsonAlias({"replyToMessageId", "parentId"})
    private String replyToMessageId;
}
