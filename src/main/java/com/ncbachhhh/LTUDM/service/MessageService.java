package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.request.MessageRequest;
import com.ncbachhhh.LTUDM.dto.response.MessageResponse;
import com.ncbachhhh.LTUDM.entity.Message.Message;
import com.ncbachhhh.LTUDM.entity.Message.MessageType;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.mapper.MessageMapper;
import com.ncbachhhh.LTUDM.repository.MessageRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageService {
    MessageRepository messageRepository;
    MessageMapper messageMapper;

    // Lấy userId từ token hiện tại
    private String getCurrentUserId() {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }

    // Gửi tin nhắn mới (từ REST API - lấy userId từ token)
    public MessageResponse sendMessage(MessageRequest request) {
        String senderId = getCurrentUserId();
        return sendMessage(request, senderId);
    }

    // Gửi tin nhắn mới (từ WebSocket - truyền userId trực tiếp)
    public MessageResponse sendMessage(MessageRequest request, String senderId) {
        // Kiểm tra nội dung tin nhắn không rỗng
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_MESSAGE);
        }

        Message message = messageMapper.toMessage(request);
        message.setSender_id(senderId);

        // Nếu không có type thì mặc định là TEXT
        if (message.getType() == null) {
            message.setType(MessageType.TEXT);
        }

        return messageMapper.toMessageResponse(messageRepository.save(message));
    }

    // Lấy danh sách tin nhắn trong conversation
    public List<MessageResponse> getMessagesByConversation(String conversationId) {
        List<Message> messages = messageRepository.findByConversationId(conversationId);
        return messageMapper.toMessageResponseList(messages);
    }

    // Lấy tin nhắn với phân trang (load thêm tin nhắn cũ)
    public Page<MessageResponse> getMessagesByConversationPaged(String conversationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findByConversationIdPaged(conversationId, pageable);
        return messages.map(messageMapper::toMessageResponse);
    }

    // Đánh dấu tin nhắn đã đọc
    public void markAsRead(String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));
        message.set_read(true);
        messageRepository.save(message);
    }

    // Đánh dấu tất cả tin nhắn trong conversation đã đọc
    public void markAllAsRead(String conversationId) {
        String userId = getCurrentUserId();
        List<Message> messages = messageRepository.findByConversationId(conversationId);

        // Chỉ đánh dấu tin nhắn của người khác gửi
        List<Message> unreadMessages = messages.stream()
                .filter(m -> !m.getSender_id().equals(userId) && !m.is_read())
                .peek(m -> m.set_read(true))
                .toList();

        if (!unreadMessages.isEmpty()) {
            messageRepository.saveAll(unreadMessages);
        }
    }

    // Xóa tin nhắn (soft delete)
    public void deleteMessage(String messageId) {
        String userId = getCurrentUserId();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        // Chỉ người gửi mới được xóa tin nhắn của mình
        if (!message.getSender_id().equals(userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        message.set_deleted(true);
        messageRepository.save(message);
    }

    // Đếm số tin nhắn chưa đọc
    public long countUnreadMessages(String conversationId) {
        String userId = getCurrentUserId();
        return messageRepository.countUnreadMessages(conversationId, userId);
    }

    // Lấy tin nhắn mới nhất của conversation
    public MessageResponse getLatestMessage(String conversationId) {
        Message message = messageRepository.findLatestMessage(conversationId);
        if (message == null) {
            return null;
        }
        return messageMapper.toMessageResponse(message);
    }
}
