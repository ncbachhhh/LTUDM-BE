package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.request.MessageRequest;
import com.ncbachhhh.LTUDM.dto.response.MessageResponse;
import com.ncbachhhh.LTUDM.entity.Conversation.Conversation;
import com.ncbachhhh.LTUDM.entity.Conversation.ConversationType;
import com.ncbachhhh.LTUDM.entity.ConversationMembers.ConversationMember;
import com.ncbachhhh.LTUDM.entity.Friendship.FriendshipStatus;
import com.ncbachhhh.LTUDM.entity.Key.MessageDeletionId;
import com.ncbachhhh.LTUDM.entity.Key.MessageReceiptId;
import com.ncbachhhh.LTUDM.entity.Message.Message;
import com.ncbachhhh.LTUDM.entity.Message.MessageType;
import com.ncbachhhh.LTUDM.entity.MessageDeletion.MessageDeletion;
import com.ncbachhhh.LTUDM.entity.MessageReceipt.MessageReceipt;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.mapper.MessageMapper;
import com.ncbachhhh.LTUDM.repository.ConversationMemberRepository;
import com.ncbachhhh.LTUDM.repository.ConversationRepository;
import com.ncbachhhh.LTUDM.repository.FriendshipRepository;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageService {
    MessageRepository messageRepository;
    MessageReceiptRepository messageReceiptRepository;
    MessageDeletionRepository messageDeletionRepository;
    ConversationMemberRepository conversationMemberRepository;
    ConversationRepository conversationRepository;
    FriendshipRepository friendshipRepository;
    MessageMapper messageMapper;
    R2StorageService r2StorageService;

    // Lấy id user hiện tại từ SecurityContext
    private String getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }

    public MessageResponse sendMessage(MessageRequest request, MultipartFile imageFile) {
        return sendMessage(request, imageFile, getCurrentUserId());
    }

    // Tạo và lưu tin nhắn mới cho conversation
    public MessageResponse sendMessage(MessageRequest request, String senderId) {
        return sendMessage(request, null, senderId);
    }

    public MessageResponse sendMessage(MessageRequest request, MultipartFile imageFile, String senderId) {
        if (!StringUtils.hasText(senderId)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        ensureCanAccessConversation(request.getConversationId(), senderId);

        Message message = messageMapper.toMessage(request);
        message.setSenderId(senderId);
        if (message.getType() == null) {
            message.setType(MessageType.TEXT);
        }
        message.setContent(resolveMessageContent(request, imageFile, senderId, message.getType()));

        return toMessageResponse(messageRepository.save(message), senderId);
    }

    private String resolveMessageContent(
            MessageRequest request,
            MultipartFile imageFile,
            String senderId,
            MessageType messageType
    ) {
        if (messageType == MessageType.IMAGE) {
            return r2StorageService.uploadMessageImage(request.getConversationId(), senderId, imageFile);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        String content = Objects.requireNonNullElse(request.getContent(), "").trim();
        if (content.isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_MESSAGE);
        }

        return content;
    }

    // Lấy danh sách tin nhắn có phân trang
    public Page<MessageResponse> getMessagesByConversationPaged(String conversationId, int page, int size) {
        String userId = getCurrentUserId();
        ensureCanAccessConversation(conversationId, userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findVisibleMessagesByConversationPaged(conversationId, userId, pageable);
        List<MessageResponse> content = messages.getContent().stream()
                .map(message -> toMessageResponse(message, userId))
                .toList();
        return new PageImpl<>(content, pageable, messages.getTotalElements());
    }

    // Đánh dấu một tin nhắn là đã đọc với user hiện tại
    public void markAsRead(String messageId) {
        String userId = getCurrentUserId();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));
        ensureCanAccessConversation(message.getConversationId(), userId);

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

    // Đánh dấu toàn bộ tin nhắn nhìn thấy được trong conversation là đã đọc
    public void markAllAsRead(String conversationId) {
        markAllAsRead(conversationId, getCurrentUserId());
    }

    public void markAllAsRead(String conversationId, String userId) {
        ensureCanAccessConversation(conversationId, userId);

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

    // Xóa mềm tin nhắn cho riêng user hiện tại
    public void deleteMessage(String messageId) {
        String userId = getCurrentUserId();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));
        ensureCanAccessConversation(message.getConversationId(), userId);

        MessageDeletionId deletionId = new MessageDeletionId(messageId, userId);
        if (!messageDeletionRepository.existsById(deletionId)) {
            MessageDeletion deletion = new MessageDeletion();
            deletion.setId(deletionId);
            deletion.setDeletedAt(LocalDateTime.now());
            messageDeletionRepository.save(deletion);
        }
    }

    // Đếm số tin nhắn chưa đọc trong conversation
    public long countUnreadMessages(String conversationId) {
        String userId = getCurrentUserId();
        ensureCanAccessConversation(conversationId, userId);

        return messageRepository.countUnreadMessages(conversationId, userId);
    }

    // Lấy tin nhắn mới nhất còn hiển thị với user hiện tại
    public MessageResponse getLatestMessage(String conversationId) {
        String userId = getCurrentUserId();
        ensureCanAccessConversation(conversationId, userId);

        Page<Message> page = messageRepository.findVisibleMessagesByConversationPaged(conversationId, userId, PageRequest.of(0, 1));
        if (page.isEmpty()) {
            return null;
        }
        return toMessageResponse(page.getContent().getFirst(), userId);
    }

    // Ánh xạ message entity sang response và bổ sung trạng thái đã đọc
    private MessageResponse toMessageResponse(Message message, String userId) {
        MessageResponse response = messageMapper.toMessageResponse(message);
        response.setRead(messageReceiptRepository.existsById(new MessageReceiptId(message.getId(), userId)));
        return response;
    }

    public void ensureCanAccessConversation(String conversationId, String userId) {
        if (!StringUtils.hasText(conversationId) || !StringUtils.hasText(userId)) {
            throw new AppException(ErrorCode.NOT_CONVERSATION_MEMBER);
        }

        if (!conversationMemberRepository.existsByIdConversationIdAndIdUserId(conversationId, userId)) {
            throw new AppException(ErrorCode.NOT_CONVERSATION_MEMBER);
        }
        ensureDirectConversationIsBetweenFriends(conversationId, userId);
    }

    private void ensureDirectConversationIsBetweenFriends(String conversationId, String userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        if (conversation.getType() != ConversationType.DIRECT) {
            return;
        }

        String otherUserId = conversationMemberRepository.findByIdConversationId(conversationId).stream()
                .map(ConversationMember::getId)
                .map(id -> id.getUserId())
                .filter(memberId -> !memberId.equals(userId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_DIRECT_CONVERSATION_MEMBERS));

        if (!friendshipRepository.existsBetweenUsersByStatus(userId, otherUserId, FriendshipStatus.ACCEPTED)) {
            throw new AppException(ErrorCode.NOT_FRIENDS);
        }
    }
}
