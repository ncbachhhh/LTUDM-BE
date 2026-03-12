package com.ncbachhhh.LTUDM.mapper;

import com.ncbachhhh.LTUDM.dto.request.MessageRequest;
import com.ncbachhhh.LTUDM.dto.response.MessageResponse;
import com.ncbachhhh.LTUDM.entity.Message.Message;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    // Chuyển từ Request sang Entity
    Message toMessage(MessageRequest request);

    // Cập nhật Entity từ Request (bỏ qua các field null)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateMessage(MessageRequest request, @MappingTarget Message message);

    // Chuyển từ Entity sang Response
    MessageResponse toMessageResponse(Message message);

    // Chuyển danh sách Entity sang danh sách Response
    List<MessageResponse> toMessageResponseList(List<Message> messages);
}
