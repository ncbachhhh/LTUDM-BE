package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.request.AddConversationMembersRequest;
import com.ncbachhhh.LTUDM.dto.request.CreateConversationRequest;
import com.ncbachhhh.LTUDM.dto.response.ConversationMemberResponse;
import com.ncbachhhh.LTUDM.dto.response.ConversationResponse;
import com.ncbachhhh.LTUDM.entity.Conversation.Conversation;
import com.ncbachhhh.LTUDM.entity.Conversation.ConversationType;
import com.ncbachhhh.LTUDM.entity.ConversationMembers.ConversationMember;
import com.ncbachhhh.LTUDM.entity.ConversationMembers.ConversationMemberRole;
import com.ncbachhhh.LTUDM.entity.Key.ConversationMemberId;
import com.ncbachhhh.LTUDM.entity.User.User;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.repository.ConversationMemberRepository;
import com.ncbachhhh.LTUDM.repository.ConversationRepository;
import com.ncbachhhh.LTUDM.repository.MessageDeletionRepository;
import com.ncbachhhh.LTUDM.repository.MessageReceiptRepository;
import com.ncbachhhh.LTUDM.repository.MessageRepository;
import com.ncbachhhh.LTUDM.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationService {
    ConversationRepository conversationRepository;
    ConversationMemberRepository conversationMemberRepository;
    MessageRepository messageRepository;
    MessageReceiptRepository messageReceiptRepository;
    MessageDeletionRepository messageDeletionRepository;
    UserRepository userRepository;

    // Tạo đoạn chat theo loại được gửi từ client
    @Transactional
    public ConversationResponse createConversation(CreateConversationRequest request) {
        String currentUserId = getCurrentUserId();
        ConversationType conversationType = parseConversationType(request.getType());

        return switch (conversationType) {
            case DIRECT -> createDirectConversation(currentUserId, request);
            case GROUP -> createGroupConversation(currentUserId, request);
        };
    }

    // Thêm một hoặc nhiều user vào nhóm nếu người gọi có quyền quản lý
    @Transactional
    public ConversationResponse addMembers(String conversationId, AddConversationMembersRequest request) {
        String currentUserId = getCurrentUserId();
        Conversation conversation = getConversation(conversationId);
        ensureGroupConversation(conversation);
        ensureCanManageGroup(conversationId, currentUserId);

        List<String> requestedMemberIds = sanitizeMemberIds(request.getMemberIds());
        if (requestedMemberIds.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_CONVERSATION_MEMBERS);
        }

        List<String> existingMemberIds = conversationMemberRepository.findByIdConversationId(conversationId).stream()
                .map(member -> member.getId().getUserId())
                .toList();
        Set<String> existingMemberIdSet = Set.copyOf(existingMemberIds);

        List<String> newMemberIds = requestedMemberIds.stream()
                .filter(memberId -> !existingMemberIdSet.contains(memberId))
                .toList();

        if (newMemberIds.isEmpty()) {
            throw new AppException(ErrorCode.MEMBER_ALREADY_IN_CONVERSATION);
        }

        Map<String, User> users = getUsersByIds(newMemberIds);
        List<ConversationMember> newMembers = newMemberIds.stream()
                .map(userId -> buildConversationMember(conversationId, userId, ConversationMemberRole.MEMBER))
                .toList();
        conversationMemberRepository.saveAll(newMembers);

        return toConversationResponse(conversation, users);
    }

    // Xóa nhóm chat và toàn bộ dữ liệu message liên quan
    @Transactional
    public void deleteGroupConversation(String conversationId) {
        String currentUserId = getCurrentUserId();
        Conversation conversation = getConversation(conversationId);
        ensureGroupConversation(conversation);
        ensureCanManageGroup(conversationId, currentUserId);

        List<String> messageIds = messageRepository.findByConversationId(conversationId).stream()
                .map(message -> message.getId())
                .toList();
        if (!messageIds.isEmpty()) {
            messageReceiptRepository.deleteByIdMessageIdIn(messageIds);
            messageDeletionRepository.deleteByIdMessageIdIn(messageIds);
            messageRepository.deleteByConversationId(conversationId);
        }
        conversationMemberRepository.deleteByIdConversationId(conversationId);
        conversationRepository.delete(conversation);
    }

    // Tạo chat cá nhân mới hoặc trả về chat đã tồn tại giữa hai user
    private ConversationResponse createDirectConversation(String currentUserId, CreateConversationRequest request) {
        List<String> memberIds = sanitizeMemberIds(request.getMemberIds());
        if (memberIds.size() != 1 || currentUserId.equals(memberIds.getFirst())) {
            throw new AppException(ErrorCode.INVALID_DIRECT_CONVERSATION_MEMBERS);
        }

        String targetUserId = memberIds.getFirst();
        getUser(targetUserId);

        Conversation existingConversation = findExistingDirectConversation(currentUserId, targetUserId);
        if (existingConversation != null) {
            return toConversationResponse(existingConversation);
        }

        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.DIRECT);
        conversation.setCreatedBy(currentUserId);
        conversation.setTitle(null);
        conversation.setAvatarUrl(null);
        Conversation savedConversation = conversationRepository.save(conversation);

        List<ConversationMember> members = List.of(
                buildConversationMember(savedConversation.getId(), currentUserId, ConversationMemberRole.OWNER),
                buildConversationMember(savedConversation.getId(), targetUserId, ConversationMemberRole.MEMBER)
        );
        conversationMemberRepository.saveAll(members);

        return toConversationResponse(savedConversation);
    }

    // Tạo nhóm chat mới và gán người tạo làm chủ nhóm
    private ConversationResponse createGroupConversation(String currentUserId, CreateConversationRequest request) {
        String title = request.getTitle() == null ? null : request.getTitle().trim();
        if (title == null || title.isEmpty()) {
            throw new AppException(ErrorCode.GROUP_TITLE_REQUIRED);
        }

        List<String> requestedMemberIds = sanitizeMemberIds(request.getMemberIds());
        Set<String> allMemberIds = new LinkedHashSet<>(requestedMemberIds);
        allMemberIds.add(currentUserId);

        if (allMemberIds.size() < 2) {
            throw new AppException(ErrorCode.INVALID_GROUP_CONVERSATION_MEMBERS);
        }

        Map<String, User> users = getUsersByIds(new ArrayList<>(allMemberIds));

        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.GROUP);
        conversation.setTitle(title);
        conversation.setAvatarUrl(request.getAvatarUrl());
        conversation.setCreatedBy(currentUserId);
        Conversation savedConversation = conversationRepository.save(conversation);

        List<ConversationMember> members = allMemberIds.stream()
                .map(userId -> buildConversationMember(
                        savedConversation.getId(),
                        userId,
                        userId.equals(currentUserId) ? ConversationMemberRole.OWNER : ConversationMemberRole.MEMBER))
                .toList();
        conversationMemberRepository.saveAll(members);

        return toConversationResponse(savedConversation, users);
    }

    // Tìm chat cá nhân hiện có của hai user để tránh tạo trùng
    private Conversation findExistingDirectConversation(String currentUserId, String targetUserId) {
        List<String> conversationIds = conversationMemberRepository.findByIdUserId(currentUserId).stream()
                .map(member -> member.getId().getConversationId())
                .distinct()
                .toList();

        if (conversationIds.isEmpty()) {
            return null;
        }

        List<Conversation> directConversations = conversationRepository.findByIdInAndType(conversationIds, ConversationType.DIRECT);
        for (Conversation conversation : directConversations) {
            List<ConversationMember> members = conversationMemberRepository.findByIdConversationId(conversation.getId());
            if (members.size() == 2) {
                Set<String> memberIds = members.stream()
                        .map(member -> member.getId().getUserId())
                        .collect(Collectors.toSet());
                if (memberIds.contains(currentUserId) && memberIds.contains(targetUserId)) {
                    return conversation;
                }
            }
        }

        return null;
    }

    // Build response conversation với dữ liệu member lấy từ database
    private ConversationResponse toConversationResponse(Conversation conversation) {
        return toConversationResponse(conversation, null);
    }

    // Ánh xạ conversation và danh sách thành viên sang DTO response
    private ConversationResponse toConversationResponse(Conversation conversation, Map<String, User> providedUsers) {
        List<ConversationMember> members = conversationMemberRepository.findByIdConversationId(conversation.getId());
        List<String> memberIds = members.stream()
                .map(member -> member.getId().getUserId())
                .toList();
        Map<String, User> users = getUsersByIds(memberIds);
        if (providedUsers != null && !providedUsers.isEmpty()) {
            users.putAll(providedUsers);
        }

        List<ConversationMemberResponse> memberResponses = members.stream()
                .sorted(Comparator.comparing(ConversationMember::getJoinedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(member -> {
                    User user = users.get(member.getId().getUserId());
                    return ConversationMemberResponse.builder()
                            .userId(member.getId().getUserId())
                            .username(user.getUsername())
                            .displayName(user.getDisplayName())
                            .avatarUrl(user.getAvatarUrl())
                            .role(member.getRole())
                            .joinedAt(member.getJoinedAt())
                            .build();
                })
                .toList();

        return ConversationResponse.builder()
                .id(conversation.getId())
                .type(conversation.getType())
                .title(conversation.getTitle())
                .createdBy(conversation.getCreatedBy())
                .createdAt(conversation.getCreatedAt())
                .avatarUrl(conversation.getAvatarUrl())
                .members(memberResponses)
                .build();
    }

    // Lấy conversation hoặc ném lỗi nếu không tồn tại
    private Conversation getConversation(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
    }

    // Chỉ cho phép các thao tác quản trị trên conversation nhóm
    private void ensureGroupConversation(Conversation conversation) {
        if (conversation.getType() != ConversationType.GROUP) {
            throw new AppException(ErrorCode.GROUP_OPERATION_NOT_ALLOWED);
        }
    }

    // Kiểm tra user hiện tại có vai trò quản lý nhóm hay không
    private void ensureCanManageGroup(String conversationId, String userId) {
        ConversationMember member = conversationMemberRepository.findById(new ConversationMemberId(conversationId, userId))
                .orElseThrow(() -> new AppException(ErrorCode.NOT_CONVERSATION_MEMBER));
        if (member.getRole() != ConversationMemberRole.OWNER && member.getRole() != ConversationMemberRole.ADMIN) {
            throw new AppException(ErrorCode.NOT_GROUP_MANAGER);
        }
    }

    // Tạo entity member cho conversation
    private ConversationMember buildConversationMember(String conversationId, String userId, ConversationMemberRole role) {
        ConversationMember member = new ConversationMember();
        member.setId(new ConversationMemberId(conversationId, userId));
        member.setRole(role);
        return member;
    }

    // Parse chuỗi type từ request sang enum ConversationType
    private ConversationType parseConversationType(String type) {
        if (type == null || type.isBlank()) {
            throw new AppException(ErrorCode.INVALID_CONVERSATION_TYPE);
        }

        try {
            return ConversationType.valueOf(type.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.INVALID_CONVERSATION_TYPE);
        }
    }

    // Làm sạch danh sách member id: bỏ null, blank và phần tử trùng
    private List<String> sanitizeMemberIds(List<String> memberIds) {
        if (memberIds == null) {
            return List.of();
        }

        return memberIds.stream()
                .filter(memberId -> memberId != null && !memberId.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    // Tải danh sách user theo id và đảm bảo không có user nào bị thiếu
    private Map<String, User> getUsersByIds(List<String> userIds) {
        List<User> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        return users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }

    // Lấy một user theo id
    private User getUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    // Lấy id user hiện tại từ SecurityContext
    private String getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }
}
