package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.request.MessageRequest;
import com.ncbachhhh.LTUDM.dto.response.AttachmentResponse;
import com.ncbachhhh.LTUDM.dto.response.ConversationLinkResponse;
import com.ncbachhhh.LTUDM.dto.response.MessageSeenByResponse;
import com.ncbachhhh.LTUDM.dto.response.MessageReplyResponse;
import com.ncbachhhh.LTUDM.dto.response.MessageResponse;
import com.ncbachhhh.LTUDM.entity.Attachment.Attachment;
import com.ncbachhhh.LTUDM.entity.Conversation.Conversation;
import com.ncbachhhh.LTUDM.entity.Conversation.ConversationType;
import com.ncbachhhh.LTUDM.entity.ConversationMembers.ConversationMember;
import com.ncbachhhh.LTUDM.entity.Key.MessageDeletionId;
import com.ncbachhhh.LTUDM.entity.Key.MessageReceiptId;
import com.ncbachhhh.LTUDM.entity.Message.Message;
import com.ncbachhhh.LTUDM.entity.Message.MessageType;
import com.ncbachhhh.LTUDM.entity.MessageDeletion.MessageDeletion;
import com.ncbachhhh.LTUDM.entity.MessageReceipt.MessageReceipt;
import com.ncbachhhh.LTUDM.entity.PinnedMessage.PinnedMessage;
import com.ncbachhhh.LTUDM.entity.User.User;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.mapper.MessageMapper;
import com.ncbachhhh.LTUDM.repository.AttachmentRepository;
import com.ncbachhhh.LTUDM.repository.ConversationMemberRepository;
import com.ncbachhhh.LTUDM.repository.ConversationRepository;
import com.ncbachhhh.LTUDM.repository.MessageDeletionRepository;
import com.ncbachhhh.LTUDM.repository.MessageReceiptRepository;
import com.ncbachhhh.LTUDM.repository.MessageRepository;
import com.ncbachhhh.LTUDM.repository.PinnedMessageRepository;
import com.ncbachhhh.LTUDM.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageService {
    MessageRepository messageRepository;
    MessageReceiptRepository messageReceiptRepository;
    MessageDeletionRepository messageDeletionRepository;
    ConversationMemberRepository conversationMemberRepository;
    ConversationRepository conversationRepository;
    AttachmentRepository attachmentRepository;
    PinnedMessageRepository pinnedMessageRepository;
    UserRepository userRepository;
    MessageMapper messageMapper;
    R2StorageService r2StorageService;
    RelationshipService relationshipService;

    static final int MAX_PINNED_MESSAGES_PER_CONVERSATION = 5;
    private static final Pattern LINK_PATTERN = Pattern.compile(
            "(?i)(?<![\\w@])((?:https?://|www\\.)?(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,63}(?:/[^\\s<]*)?)"
    );

    // Lấy current user id từ SecurityContext cho các REST workflow.
    private String getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }

    // Gửi message từ REST multipart, sender lấy từ JWT.
    public MessageResponse sendMessage(MessageRequest request, MultipartFile imageFile) {
        return sendMessage(request, imageFile, getCurrentUserId());
    }

    // Gửi message từ WebSocket, sender đã được resolve từ Principal.
    public MessageResponse sendMessage(MessageRequest request, String senderId) {
        return sendMessage(request, null, senderId);
    }

    // Workflow gửi message chung: check quyền, resolve content/file/reply, save message và attachment.
    public MessageResponse sendMessage(MessageRequest request, MultipartFile file, String senderId) {
        if (!StringUtils.hasText(senderId)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        ensureCanSendMessage(request.getConversationId(), senderId);

        Message message = messageMapper.toMessage(request);
        message.setSenderId(senderId);
        if (message.getType() == null) {
            message.setType(MessageType.TEXT);
        }
        // Reply chỉ hợp lệ khi message gốc visible với sender và cùng conversation.
        message.setReplyToMessageId(resolveReplyToMessageId(request, senderId));
        message.setContent(resolveMessageContent(request, file, senderId, message.getType()));

        Message savedMessage = messageRepository.save(message);
        Attachment attachment = createAttachmentIfNeeded(savedMessage, file);

        return toMessageResponse(savedMessage, senderId, attachment);
    }

    // Chuyển request/file thanh content lưu DB: text giu content, image/file upload R2 và lưu URL.
    private String resolveMessageContent(
            MessageRequest request,
            MultipartFile file,
            String senderId,
            MessageType messageType
    ) {
        if (messageType == MessageType.IMAGE) {
            return r2StorageService.uploadMessageImage(request.getConversationId(), senderId, file);
        }

        if (messageType == MessageType.FILE) {
            return r2StorageService.uploadMessageFile(request.getConversationId(), senderId, file);
        }

        // TEXT message không được kèm multipart file để tránh sai type/content.
        if (file != null && !file.isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        String content = Objects.requireNonNullElse(request.getContent(), "").trim();
        if (content.isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_MESSAGE);
        }

        return content;
    }

    // Lấy message visible theo trang và batch load attachment/pin để tránh query từng item.
    public Page<MessageResponse> getMessagesByConversationPaged(String conversationId, int page, int size) {
        String userId = getCurrentUserId();
        ensureCanAccessConversation(conversationId, userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findVisibleMessagesByConversationPaged(conversationId, userId, pageable);
        Map<String, Attachment> attachmentsByMessageId = getAttachmentsByMessageId(messages.getContent());
        Map<String, PinnedMessage> pinnedByMessageId = getPinnedMessagesByMessageId(messages.getContent());
        List<MessageResponse> content = messages.getContent().stream()
                .map(message -> toMessageResponse(
                        message,
                        userId,
                        attachmentsByMessageId.get(message.getId()),
                        pinnedByMessageId.get(message.getId())))
                .toList();
        return new PageImpl<>(content, pageable, messages.getTotalElements());
    }

    // Search text message visible trong conversation, có validate keyword tối thiểu.
    public Page<MessageResponse> searchMessages(String conversationId, String keyword, int page, int size) {
        String userId = getCurrentUserId();
        ensureCanAccessConversation(conversationId, userId);

        if (!StringUtils.hasText(keyword) || keyword.trim().length() < 2) {
            throw new AppException(ErrorCode.SEARCH_QUERY_REQUIRED);
        }

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 50)));
        Page<Message> messages = messageRepository.searchVisibleTextMessages(
                conversationId,
                userId,
                keyword.trim(),
                pageable
        );
        Map<String, Attachment> attachmentsByMessageId = getAttachmentsByMessageId(messages.getContent());
        Map<String, PinnedMessage> pinnedByMessageId = getPinnedMessagesByMessageId(messages.getContent());
        List<MessageResponse> content = messages.getContent().stream()
                .map(message -> toMessageResponse(
                        message,
                        userId,
                        attachmentsByMessageId.get(message.getId()),
                        pinnedByMessageId.get(message.getId())))
                .toList();
        return new PageImpl<>(content, pageable, messages.getTotalElements());
    }

    // Pin message visible, nếu chưa pin và chưa vượt giới hạn pin của conversation.
    @Transactional
    public MessageResponse pinMessage(String messageId) {
        String userId = getCurrentUserId();
        Message message = getVisibleMessageForUser(messageId, userId);
        ensureCanAccessConversation(message.getConversationId(), userId);

        PinnedMessage pinnedMessage = pinnedMessageRepository.findByMessageId(messageId).orElse(null);
        if (pinnedMessage == null) {
            // Giới hạn pin để UI không phải xử lý danh sách pin quá dài.
            if (pinnedMessageRepository.countByConversationId(message.getConversationId()) >= MAX_PINNED_MESSAGES_PER_CONVERSATION) {
                throw new AppException(ErrorCode.PINNED_MESSAGE_LIMIT_EXCEEDED);
            }

            pinnedMessage = new PinnedMessage();
            pinnedMessage.setMessageId(message.getId());
            pinnedMessage.setConversationId(message.getConversationId());
            pinnedMessage.setPinnedBy(userId);
            pinnedMessage.setPinnedAt(LocalDateTime.now());
            pinnedMessage = pinnedMessageRepository.save(pinnedMessage);
        }

        return toMessageResponse(message, userId, getAttachment(messageId), pinnedMessage);
    }

    // Gỡ pin message nếu current user có quyền vào conversation.
    @Transactional
    public MessageResponse unpinMessage(String messageId) {
        String userId = getCurrentUserId();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));
        ensureCanAccessConversation(message.getConversationId(), userId);
        pinnedMessageRepository.deleteById(messageId);
        return toMessageResponse(message, userId, getAttachment(messageId), null);
    }

    // Thu hồi message: chỉ sender được recall, content/attachment sẽ bị ẩn trong response.
    @Transactional
    public MessageResponse recallMessage(String messageId) {
        String userId = getCurrentUserId();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));
        ensureCanAccessConversation(message.getConversationId(), userId);

        if (!message.getSenderId().equals(userId)) {
            throw new AppException(ErrorCode.CANNOT_RECALL_OTHERS_MESSAGE);
        }

        if (!message.isRecalled()) {
            message.setRecalled(true);
            message.setRecalledAt(LocalDateTime.now());
            message.setRecalledBy(userId);
            message = messageRepository.save(message);
        }

        return toMessageResponse(message, userId);
    }

    // Lấy danh sách pinned message visible với current user theo thứ tự pin mới nhất.
    public List<MessageResponse> getPinnedMessages(String conversationId) {
        String userId = getCurrentUserId();
        ensureCanAccessConversation(conversationId, userId);

        List<PinnedMessage> pinnedMessages = pinnedMessageRepository.findByConversationIdOrderByPinnedAtDesc(conversationId);
        if (pinnedMessages.isEmpty()) {
            return List.of();
        }

        Map<String, PinnedMessage> pinnedByMessageId = pinnedMessages.stream()
                .collect(Collectors.toMap(PinnedMessage::getMessageId, pinnedMessage -> pinnedMessage));

        List<String> messageIds = pinnedMessages.stream()
                .map(PinnedMessage::getMessageId)
                .toList();

        Map<String, Message> messagesById = messageRepository.findAllById(messageIds).stream()
                .filter(message -> !isDeletedForUser(message.getId(), userId))
                .collect(Collectors.toMap(Message::getId, message -> message));

        // Batch load attachment cho pinned messages để map response không query lặp lại.
        Map<String, Attachment> attachmentsByMessageId = getAttachmentsByMessageId(messagesById.values().stream().toList());

        return pinnedMessages.stream()
                .map(PinnedMessage::getMessageId)
                .map(messagesById::get)
                .filter(Objects::nonNull)
                .map(message -> toMessageResponse(
                        message,
                        userId,
                        attachmentsByMessageId.get(message.getId()),
                        pinnedByMessageId.get(message.getId())))
                .toList();
    }

    // Lấy media image trong conversation.
    public Page<MessageResponse> getConversationImages(String conversationId, int page, int size) {
        return getConversationMessagesByType(conversationId, MessageType.IMAGE, page, size);
    }

    // Lấy media file trong conversation.
    public Page<MessageResponse> getConversationFiles(String conversationId, int page, int size) {
        return getConversationMessagesByType(conversationId, MessageType.FILE, page, size);
    }

    // Lấy một số image gần nhất để hiện preview nhanh trong info panel.
    public List<MessageResponse> getConversationImagePreview(String conversationId, int limit) {
        String userId = getCurrentUserId();
        ensureCanAccessConversation(conversationId, userId);

        int safeLimit = Math.max(1, Math.min(limit, 20));
        Page<Message> messages = messageRepository.findVisibleMessagesByConversationAndTypePaged(
                conversationId,
                userId,
                MessageType.IMAGE,
                PageRequest.of(0, safeLimit)
        );
        Map<String, Attachment> attachmentsByMessageId = getAttachmentsByMessageId(messages.getContent());
        Map<String, PinnedMessage> pinnedByMessageId = getPinnedMessagesByMessageId(messages.getContent());

        return messages.getContent().stream()
                .map(message -> toMessageResponse(
                        message,
                        userId,
                        attachmentsByMessageId.get(message.getId()),
                        pinnedByMessageId.get(message.getId())))
                .toList();
    }

    // Trích xuất link từ các text message visible và phân trang trên danh sách link đã parse.
    public Page<ConversationLinkResponse> getConversationLinks(String conversationId, int page, int size) {
        String userId = getCurrentUserId();
        ensureCanAccessConversation(conversationId, userId);

        List<ConversationLinkResponse> links = messageRepository.findVisibleLinkCandidateMessages(conversationId, userId).stream()
                .flatMap(message -> extractLinks(message).stream())
                .toList();

        Pageable pageable = PageRequest.of(page, size);
        int start = (int) Math.min(pageable.getOffset(), links.size());
        int end = Math.min(start + pageable.getPageSize(), links.size());
        return new PageImpl<>(links.subList(start, end), pageable, links.size());
    }

    // Đánh dấu một message đã đọc cho current user; sender từ đọc message của mình thì không tạo receipt.
    public MessageSeenByResponse markAsRead(String messageId) {
        String userId = getCurrentUserId();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));
        ensureCanAccessConversation(message.getConversationId(), userId);

        if (message.getSenderId().equals(userId)) {
            return null;
        }

        MessageReceiptId receiptId = new MessageReceiptId(messageId, userId);
        if (!messageReceiptRepository.existsById(receiptId)) {
            MessageReceipt receipt = new MessageReceipt();
            receipt.setId(receiptId);
            receipt.setSeenAt(LocalDateTime.now());
            messageReceiptRepository.save(receipt);
            return toSeenByResponse(receipt, message.getConversationId());
        }
        return messageReceiptRepository.findById(receiptId)
                .map(receipt -> toSeenByResponse(receipt, message.getConversationId()))
                .orElse(null);
    }

    // Mark all read cho current user.
    public List<MessageReceipt> markAllAsRead(String conversationId) {
        return markAllAsRead(conversationId, getCurrentUserId());
    }

    // Tạo receipt cho tất cả visible messages chưa đọc, bỏ qua message do chính user gửi.
    public List<MessageReceipt> markAllAsRead(String conversationId, String userId) {
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
        return newReceipts;
    }

    // Soft delete message riêng cho current user bằng MessageDeletion.
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

    // Đếm unread message của conversation theo current user.
    public long countUnreadMessages(String conversationId) {
        String userId = getCurrentUserId();
        ensureCanAccessConversation(conversationId, userId);

        return messageRepository.countUnreadMessages(conversationId, userId);
    }

    // Lấy message mới nhất visible với current user.
    public MessageResponse getLatestMessage(String conversationId) {
        String userId = getCurrentUserId();
        ensureCanAccessConversation(conversationId, userId);

        Page<Message> page = messageRepository.findVisibleMessagesByConversationPaged(conversationId, userId, PageRequest.of(0, 1));
        if (page.isEmpty()) {
            return null;
        }
        return toMessageResponse(page.getContent().getFirst(), userId);
    }

    // Map message sang response và tự load attachment/pin nếu cần.
    private MessageResponse toMessageResponse(Message message, String userId) {
        return toMessageResponse(message, userId, getAttachment(message.getId()), getPinnedMessage(message.getId()));
    }

    // Map message sang response với attachment đã có sẵn.
    private MessageResponse toMessageResponse(Message message, String userId, Attachment attachment) {
        return toMessageResponse(message, userId, attachment, getPinnedMessage(message.getId()));
    }

    // Map message sang response đầy đủ, xử lý recalled, read state, seen by, pin và reply preview.
    private MessageResponse toMessageResponse(Message message, String userId, Attachment attachment, PinnedMessage pinnedMessage) {
        MessageResponse response = messageMapper.toMessageResponse(message);
        if (message.isRecalled()) {
            // Message đã recall không trả content/attachment nữa để client không hien nội dung cũ.
            response.setContent(null);
            attachment = null;
        }
        response.setRead(messageReceiptRepository.existsById(new MessageReceiptId(message.getId(), userId)));
        response.setSeenBy(buildSeenByResponses(message));
        response.setAttachment(toAttachmentResponse(attachment));
        response.setPinned(pinnedMessage != null);
        response.setPinnedBy(pinnedMessage == null ? null : pinnedMessage.getPinnedBy());
        response.setPinnedAt(pinnedMessage == null ? null : pinnedMessage.getPinnedAt());
        response.setReplyToMessage(buildReplyResponse(message.getReplyToMessageId(), userId));
        return response;
    }

    // Map receipt sang seen-by DTO, gồm thêm profile và nickname trong conversation.
    public MessageSeenByResponse toSeenByResponse(MessageReceipt receipt, String conversationId) {
        if (receipt == null || receipt.getId() == null) {
            return null;
        }

        String readerId = receipt.getId().getUserId();
        User user = userRepository.findById(readerId).orElse(null);
        ConversationMember member = conversationMemberRepository
                .findByIdConversationId(conversationId)
                .stream()
                .filter(candidate -> candidate.getId().getUserId().equals(readerId))
                .findFirst()
                .orElse(null);

        return MessageSeenByResponse.builder()
                .userId(readerId)
                .username(user == null ? null : user.getUsername())
                .displayName(user == null ? null : user.getDisplayName())
                .nickname(member == null ? null : member.getNickname())
                .avatarUrl(user == null ? null : user.getAvatarUrl())
                .seenAt(receipt.getSeenAt())
                .build();
    }

    // Build danh sách user đã đọc message, bỏ qua sender.
    private List<MessageSeenByResponse> buildSeenByResponses(Message message) {
        return messageReceiptRepository.findByIdMessageId(message.getId()).stream()
                .filter(receipt -> !receipt.getId().getUserId().equals(message.getSenderId()))
                .map(receipt -> toSeenByResponse(receipt, message.getConversationId()))
                .filter(Objects::nonNull)
                .toList();
    }

    // Build preview message được reply, null nếu reply target đã bị xóa với user hiện tại.
    private MessageReplyResponse buildReplyResponse(String replyToMessageId, String userId) {
        if (!StringUtils.hasText(replyToMessageId) || isDeletedForUser(replyToMessageId, userId)) {
            return null;
        }

        return messageRepository.findById(replyToMessageId)
                .map(message -> MessageReplyResponse.builder()
                        .id(message.getId())
                        .senderId(message.getSenderId())
                        .type(message.getType())
                        .content(message.isRecalled() ? null : message.getContent())
                        .recalled(message.isRecalled())
                        .build())
                .orElse(null);
    }

    // Lấy image/file messages theo type, batch load attachment/pin rồi map thành page response.
    private Page<MessageResponse> getConversationMessagesByType(
            String conversationId,
            MessageType type,
            int page,
            int size
    ) {
        String userId = getCurrentUserId();
        ensureCanAccessConversation(conversationId, userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findVisibleMessagesByConversationAndTypePaged(
                conversationId,
                userId,
                type,
                pageable
        );
        Map<String, Attachment> attachmentsByMessageId = getAttachmentsByMessageId(messages.getContent());
        Map<String, PinnedMessage> pinnedByMessageId = getPinnedMessagesByMessageId(messages.getContent());
        List<MessageResponse> content = messages.getContent().stream()
                .map(message -> toMessageResponse(
                        message,
                        userId,
                        attachmentsByMessageId.get(message.getId()),
                        pinnedByMessageId.get(message.getId())))
                .toList();

        return new PageImpl<>(content, pageable, messages.getTotalElements());
    }

    // Trich link từ một text message và tạo DTO cho từng link tìm thấy.
    private List<ConversationLinkResponse> extractLinks(Message message) {
        if (!StringUtils.hasText(message.getContent())) {
            return List.of();
        }

        List<ConversationLinkResponse> links = new ArrayList<>();
        Matcher matcher = LINK_PATTERN.matcher(message.getContent());
        while (matcher.find()) {
            String url = trimTrailingUrlPunctuation(matcher.group(1));
            // Bỏ qua fragment trong email và các match rỗng sau khi trim dấu câu.
            if (!StringUtils.hasText(url) || isLikelyEmailFragment(message.getContent(), matcher.start(1))) {
                continue;
            }

            links.add(ConversationLinkResponse.builder()
                    .messageId(message.getId())
                    .conversationId(message.getConversationId())
                    .senderId(message.getSenderId())
                    .url(url)
                    .normalizedUrl(normalizeUrl(url))
                    .text(message.getContent())
                    .createdAt(message.getCreatedAt())
                    .build());
        }
        return links;
    }

    // Chuẩn hóa URL thiếu protocol để FE có link click được.
    private String normalizeUrl(String url) {
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.startsWith("http://") || lowerUrl.startsWith("https://")) {
            return url;
        }
        return "https://" + url;
    }

    // Phân biệt domain trong email với URL thật sự.
    private boolean isLikelyEmailFragment(String content, int matchStart) {
        return matchStart > 0 && content.charAt(matchStart - 1) == '@';
    }

    // Loại dấu câu ở cuối URL như dấu chấm/dấu ngoặc khi URL nằm trong câu văn.
    private String trimTrailingUrlPunctuation(String url) {
        String trimmed = url;
        while (!trimmed.isEmpty() && ".,;:!?)\\]}\"'".indexOf(trimmed.charAt(trimmed.length() - 1)) >= 0) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    // Validate reply target: phải visible, cùng conversation và chưa bị recall.
    private String resolveReplyToMessageId(MessageRequest request, String senderId) {
        String replyToMessageId = request.getReplyToMessageId();
        if (!StringUtils.hasText(replyToMessageId)) {
            return null;
        }

        Message repliedMessage = getVisibleMessageForUser(replyToMessageId, senderId);
        if (!Objects.equals(repliedMessage.getConversationId(), request.getConversationId())) {
            throw new AppException(ErrorCode.MESSAGE_NOT_FOUND);
        }
        if (repliedMessage.isRecalled()) {
            throw new AppException(ErrorCode.CANNOT_REPLY_RECALLED_MESSAGE);
        }

        return repliedMessage.getId();
    }

    // Tạo attachment metadata nếu message type là IMAGE/FILE.
    private Attachment createAttachmentIfNeeded(Message message, MultipartFile file) {
        if (!hasAttachment(message)) {
            return null;
        }

        Attachment attachment = new Attachment();
        attachment.setMessageId(message.getId());
        attachment.setFileUrl(message.getContent());
        attachment.setFileName(resolveOriginalFilename(file));
        attachment.setMimeType(file.getContentType());
        attachment.setFileSize(file.getSize());
        return attachmentRepository.save(attachment);
    }

    // Lấy tên file gốc từ multipart, fallback nếu client không gửi filename.
    private String resolveOriginalFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            return "attachment";
        }
        return originalFilename.trim();
    }

    // Load attachment theo message id, null nếu text message hoặc không có attachment.
    private Attachment getAttachment(String messageId) {
        return attachmentRepository.findByMessageId(messageId).orElse(null);
    }

    // Batch load attachments cho danh sách messages có type IMAGE/FILE.
    private Map<String, Attachment> getAttachmentsByMessageId(List<Message> messages) {
        List<String> messageIds = messages.stream()
                .filter(this::hasAttachment)
                .map(Message::getId)
                .toList();
        if (messageIds.isEmpty()) {
            return Map.of();
        }

        return attachmentRepository.findByMessageIdIn(messageIds).stream()
                .collect(Collectors.toMap(Attachment::getMessageId, attachment -> attachment));
    }

    // Batch load pinned records cho danh sách messages.
    private Map<String, PinnedMessage> getPinnedMessagesByMessageId(List<Message> messages) {
        List<String> messageIds = messages.stream()
                .map(Message::getId)
                .toList();
        if (messageIds.isEmpty()) {
            return Map.of();
        }

        return pinnedMessageRepository.findByMessageIdIn(messageIds).stream()
                .collect(Collectors.toMap(PinnedMessage::getMessageId, pinnedMessage -> pinnedMessage));
    }

    // Load pinned record của một message.
    private PinnedMessage getPinnedMessage(String messageId) {
        return pinnedMessageRepository.findByMessageId(messageId).orElse(null);
    }

    // Message có attachment khi type là IMAGE hoặc FILE.
    private boolean hasAttachment(Message message) {
        return message.getType() == MessageType.IMAGE || message.getType() == MessageType.FILE;
    }

    // Load message nếu nó còn visible với user, ngược lại trả MESSAGE_NOT_FOUND.
    private Message getVisibleMessageForUser(String messageId, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));
        if (isDeletedForUser(messageId, userId)) {
            throw new AppException(ErrorCode.MESSAGE_NOT_FOUND);
        }
        return message;
    }

    // Kiểm tra message đã bị soft delete riêng cho user chưa.
    private boolean isDeletedForUser(String messageId, String userId) {
        return messageDeletionRepository.existsById(new MessageDeletionId(messageId, userId));
    }

    // Map attachment entity sang response DTO.
    private AttachmentResponse toAttachmentResponse(Attachment attachment) {
        if (attachment == null) {
            return null;
        }

        return AttachmentResponse.builder()
                .id(attachment.getId())
                .fileUrl(attachment.getFileUrl())
                .fileName(attachment.getFileName())
                .mimeType(attachment.getMimeType())
                .fileSize(attachment.getFileSize())
                .build();
    }

    // Guard public cho controller/websocket: user phải là member của conversation.
    public void ensureCanAccessConversation(String conversationId, String userId) {
        if (!StringUtils.hasText(conversationId) || !StringUtils.hasText(userId)) {
            throw new AppException(ErrorCode.NOT_CONVERSATION_MEMBER);
        }

        if (!conversationMemberRepository.existsByIdConversationIdAndIdUserId(conversationId, userId)) {
            throw new AppException(ErrorCode.NOT_CONVERSATION_MEMBER);
        }
    }

    // Guard khi gửi message: phải truy cập được conversation và direct chat phải còn là bạn bè.
    private void ensureCanSendMessage(String conversationId, String userId) {
        ensureCanAccessConversation(conversationId, userId);
        ensureDirectConversationIsBetweenFriends(conversationId, userId);
    }

    // Direct conversation chỉ cho gửi nếu hai user vẫn ACCEPTED friends.
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

        relationshipService.ensureAcceptedFriendship(userId, otherUserId);
    }
}
