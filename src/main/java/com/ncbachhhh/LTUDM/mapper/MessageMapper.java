package com.ncbachhhh.LTUDM.mapper;

import com.ncbachhhh.LTUDM.dto.request.MessageRequest;
import com.ncbachhhh.LTUDM.dto.response.MessageResponse;
import com.ncbachhhh.LTUDM.entity.Message.Message;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

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

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senderId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "edited", ignore = true)
    @Mapping(target = "editedAt", ignore = true)
    @Mapping(target = "recalled", ignore = true)
    @Mapping(target = "recalledAt", ignore = true)
    @Mapping(target = "recalledBy", ignore = true)
    void updateMessage(MessageRequest request, @MappingTarget Message message);

    @Mapping(target = "read", ignore = true)
    MessageResponse toMessageResponse(Message message);

    List<MessageResponse> toMessageResponseList(List<Message> messages);
}
