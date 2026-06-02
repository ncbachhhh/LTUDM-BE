package com.ncbachhhh.LTUDM.mapper;

import com.ncbachhhh.LTUDM.dto.request.MessageRequest;
import com.ncbachhhh.LTUDM.dto.response.MessageResponse;
import com.ncbachhhh.LTUDM.entity.Message.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senderId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "edited", ignore = true)
    @Mapping(target = "editedAt", ignore = true)
    @Mapping(target = "recalled", ignore = true)
    @Mapping(target = "recalledAt", ignore = true)
    @Mapping(target = "recalledBy", ignore = true)
    Message toMessage(MessageRequest request);

    @Mapping(target = "read", ignore = true)
    @Mapping(target = "attachment", ignore = true)
    @Mapping(target = "pinned", ignore = true)
    @Mapping(target = "pinnedBy", ignore = true)
    @Mapping(target = "pinnedAt", ignore = true)
    MessageResponse toMessageResponse(Message message);
}
