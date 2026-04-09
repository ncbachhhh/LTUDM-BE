package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.request.MessageRequest;
import com.ncbachhhh.LTUDM.dto.response.MessageResponse;
import com.ncbachhhh.LTUDM.entity.Key.MessageDeletionId;
import com.ncbachhhh.LTUDM.entity.Key.MessageReceiptId;
import com.ncbachhhh.LTUDM.entity.Message.Message;
import com.ncbachhhh.LTUDM.entity.Message.MessageType;
import com.ncbachhhh.LTUDM.entity.MessageDeletion.MessageDeletion;
import com.ncbachhhh.LTUDM.entity.MessageReceipt.MessageReceipt;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.mapper.MessageMapper;
import com.ncbachhhh.LTUDM.repository.MessageDeletionRepository;
import com.ncbachhhh.LTUDM.repository.MessageReceiptRepository;
import com.ncbachhhh.LTUDM.repository.MessageRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageService {
    MessageRepository messageRepository;
    MessageReceiptRepository messageReceiptRepository;
    MessageDeletionRepository messageDeletionRepository;
    MessageMapper messageMapper;

    private String getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }

    public MessageResponse sendMessage(MessageRequest request) {
        return sendMessage(request, getCurrentUserId());
    }

    public MessageResponse sendMessage(MessageRequest request, String senderId) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_MESSAGE);
        }

        Message message = messageMapper.toMessage(request);
        message.setSenderId(senderId);
        if (message.getType() == null) {
            message.setType(MessageType.TEXT);
        }

        return toMessageResponse(messageRepository.save(message), senderId);
    }

    public List<MessageResponse> getMessagesByConversation(String conversationId) {
        String userId = getCurrentUserId();
        return messageRepository.findVisibleMessagesByConversation(conversationId, userId).stream()
                .map(message -> toMessageResponse(message, userId))
                .toList();
    }

    public Page<MessageResponse> getMessagesByConversationPaged(String conversationId, int page, int size) {
        String userId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findVisibleMessagesByConversationPaged(conversationId, userId, pageable);
        List<MessageResponse> content = messages.getContent().stream()
                .map(message -> toMessageResponse(message, userId))
                .toList();
        return new PageImpl<>(content, pageable, messages.getTotalElements());
    }

    public void markAsRead(String messageId) {
        String userId = getCurrentUserId();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        if (message.getSenderId().equals(userId)) {
            return;
        }

        MessageReceiptId receiptId = new MessageReceiptId(messageId, userId);
        if (!messageReceiptRepository.existsById(receiptId)) {
            MessageReceipt receipt = new MessageReceipt();
            receipt.setId(receiptId);
            receipt.setSeenAt(LocalDateTime.now());
            messageReceiptRepository.save(receipt);
        }
    }

    public void markAllAsRead(String conversationId) {
        String userId = getCurrentUserId();
        List<Message> messages = messageRepository.findVisibleMessagesByConversation(conversationId, userId);

        List<MessageReceipt> newReceipts = messages.stream()
                .filter(message -> !message.getSenderId().equals(userId))
                .filter(message -> !messageReceiptRepository.existsById(new MessageReceiptId(message.getId(), userId)))
                .map(message -> {
                    MessageReceipt receipt = new MessageReceipt();
                    receipt.setId(new MessageReceiptId(message.getId(), userId));
                    receipt.setSeenAt(LocalDateTime.now());
                    return receipt;
                })
                .toList();

        if (!newReceipts.isEmpty()) {
            messageReceiptRepository.saveAll(newReceipts);
        }
    }

    public void deleteMessage(String messageId) {
        String userId = getCurrentUserId();
        messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        MessageDeletionId deletionId = new MessageDeletionId(messageId, userId);
        if (!messageDeletionRepository.existsById(deletionId)) {
            MessageDeletion deletion = new MessageDeletion();
            deletion.setId(deletionId);
            deletion.setDeletedAt(LocalDateTime.now());
            messageDeletionRepository.save(deletion);
        }
    }

    public long countUnreadMessages(String conversationId) {
        return messageRepository.countUnreadMessages(conversationId, getCurrentUserId());
    }

    public MessageResponse getLatestMessage(String conversationId) {
        String userId = getCurrentUserId();
        Page<Message> page = messageRepository.findVisibleMessagesByConversationPaged(conversationId, userId, PageRequest.of(0, 1));
        if (page.isEmpty()) {
            return null;
        }
        return toMessageResponse(page.getContent().getFirst(), userId);
    }

    private MessageResponse toMessageResponse(Message message, String userId) {
        MessageResponse response = messageMapper.toMessageResponse(message);
        response.setRead(messageReceiptRepository.existsById(new MessageReceiptId(message.getId(), userId)));
        return response;
    }
}
