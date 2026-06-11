package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.request.AddConversationMembersRequest;
import com.ncbachhhh.LTUDM.dto.request.CreateConversationRequest;
import com.ncbachhhh.LTUDM.dto.request.MuteConversationRequest;
import com.ncbachhhh.LTUDM.dto.request.UpdateConversationEmojiRequest;
import com.ncbachhhh.LTUDM.dto.request.UpdateConversationNicknameRequest;
import com.ncbachhhh.LTUDM.dto.response.AttachmentResponse;
import com.ncbachhhh.LTUDM.dto.response.ConversationInfoResponse;
import com.ncbachhhh.LTUDM.dto.response.ConversationInfoStatResponse;
import com.ncbachhhh.LTUDM.dto.response.ConversationMemberResponse;
import com.ncbachhhh.LTUDM.dto.response.ConversationRealtimeEventResponse;
import com.ncbachhhh.LTUDM.dto.response.ConversationResponse;
import com.ncbachhhh.LTUDM.dto.response.MessageResponse;
import com.ncbachhhh.LTUDM.entity.Attachment.Attachment;
import com.ncbachhhh.LTUDM.entity.Conversation.Conversation;
import com.ncbachhhh.LTUDM.entity.Conversation.ConversationType;
import com.ncbachhhh.LTUDM.entity.ConversationDeletion.ConversationDeletion;
import com.ncbachhhh.LTUDM.entity.ConversationMembers.ConversationMember;
import com.ncbachhhh.LTUDM.entity.ConversationMembers.ConversationMemberRole;
import com.ncbachhhh.LTUDM.entity.Key.ConversationDeletionId;
import com.ncbachhhh.LTUDM.entity.Key.ConversationMemberId;
import com.ncbachhhh.LTUDM.entity.Key.MessageReceiptId;
import com.ncbachhhh.LTUDM.entity.Key.MessageDeletionId;
import com.ncbachhhh.LTUDM.entity.Message.MessageType;
import com.ncbachhhh.LTUDM.entity.Message.Message;
import com.ncbachhhh.LTUDM.entity.MessageDeletion.MessageDeletion;
import com.ncbachhhh.LTUDM.entity.User.User;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.repository.ConversationMemberRepository;
import com.ncbachhhh.LTUDM.repository.ConversationRepository;
import com.ncbachhhh.LTUDM.repository.ConversationDeletionRepository;
import com.ncbachhhh.LTUDM.repository.AttachmentRepository;
import com.ncbachhhh.LTUDM.repository.MessageDeletionRepository;
import com.ncbachhhh.LTUDM.repository.MessageReceiptRepository;
import com.ncbachhhh.LTUDM.repository.MessageRepository;
import com.ncbachhhh.LTUDM.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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
    private static final String EVENT_UPSERT = "CONVERSATION_UPSERT";
    private static final String EVENT_REMOVED = "CONVERSATION_REMOVED";
    private static final String CONVERSATION_QUEUE = "/queue/conversations";

    ConversationRepository conversationRepository;
    ConversationDeletionRepository conversationDeletionRepository;
    ConversationMemberRepository conversationMemberRepository;
    MessageRepository messageRepository;
    MessageReceiptRepository messageReceiptRepository;
    MessageDeletionRepository messageDeletionRepository;
    AttachmentRepository attachmentRepository;
    UserRepository userRepository;
    PresenceService presenceService;
    RelationshipService relationshipService;
    R2StorageService r2StorageService;
    SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ConversationResponse createConversation(CreateConversationRequest request) {
        String currentUserId = getCurrentUserId();
        ConversationType conversationType = parseConversationType(request.getType());

        ConversationResponse response = switch (conversationType) {
            case DIRECT -> createDirectConversation(currentUserId, request);
            case GROUP -> createGroupConversation(currentUserId, request);
        };
        publishConversationUpsertToMembers(response.getId(), currentUserId);
        return response;
    }

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
        ensureAllAreFriends(currentUserId, newMemberIds);

        Map<String, User> users = getUsersByIds(newMemberIds);
        List<ConversationMember> newMembers = newMemberIds.stream()
                .map(userId -> buildConversationMember(conversationId, userId, ConversationMemberRole.MEMBER))
                .toList();
        conversationMemberRepository.saveAll(newMembers);

        ConversationResponse response = toConversationResponse(conversation, users);
        publishConversationUpsert(conversationId, currentUserId, null, getConversationMemberIds(conversationId));
        return response;
    }

    @Transactional
    public void deleteGroupConversation(String conversationId) {
        String currentUserId = getCurrentUserId();
        Conversation conversation = getConversation(conversationId);
        ensureGroupConversation(conversation);
        ensureCanManageGroup(conversationId, currentUserId);

        List<String> memberIds = getConversationMemberIds(conversationId);
        deleteConversationData(conversation);
        publishConversationRemoved(conversationId, currentUserId, null, memberIds);
    }

    @Transactional
    public ConversationResponse removeGroupMember(String conversationId, String memberId) {
        String currentUserId = getCurrentUserId();
        Conversation conversation = getConversation(conversationId);
        ensureGroupConversation(conversation);
        ensureCanManageGroup(conversationId, currentUserId);

        if (currentUserId.equals(memberId)) {
            throw new AppException(ErrorCode.CANNOT_REMOVE_SELF_FROM_GROUP);
        }

        ConversationMember targetMember = getConversationMember(conversationId, memberId);
        if (targetMember.getRole() == ConversationMemberRole.OWNER) {
            throw new AppException(ErrorCode.CANNOT_REMOVE_GROUP_OWNER);
        }

        List<String> memberIdsBeforeRemoval = getConversationMemberIds(conversationId);
        conversationMemberRepository.delete(targetMember);
        List<String> remainingMemberIds = memberIdsBeforeRemoval.stream()
                .filter(userId -> !userId.equals(memberId))
                .toList();

        ConversationResponse response = toConversationResponse(conversation);
        publishConversationUpsert(conversationId, currentUserId, memberId, remainingMemberIds);
        publishConversationRemoved(conversationId, currentUserId, memberId, List.of(memberId));
        return response;
    }

    @Transactional
    public ConversationResponse transferGroupOwnership(String conversationId, String newOwnerId) {
        String currentUserId = getCurrentUserId();
        Conversation conversation = getConversation(conversationId);
        ensureGroupConversation(conversation);
        ensureCanManageGroup(conversationId, currentUserId);

        if (currentUserId.equals(newOwnerId)) {
            throw new AppException(ErrorCode.CANNOT_TRANSFER_OWNERSHIP_TO_SELF);
        }

        ConversationMember currentOwner = getConversationMember(conversationId, currentUserId);
        ConversationMember newOwner = getConversationMember(conversationId, newOwnerId);

        currentOwner.setRole(ConversationMemberRole.MEMBER);
        newOwner.setRole(ConversationMemberRole.OWNER);
        conversation.setCreatedBy(newOwnerId);

        conversationMemberRepository.saveAll(List.of(currentOwner, newOwner));
        ConversationResponse response = toConversationResponse(conversationRepository.save(conversation));
        publishConversationUpsert(conversationId, currentUserId, newOwnerId, getConversationMemberIds(conversationId));
        return response;
    }

    @Transactional
    public ConversationResponse leaveGroupConversation(String conversationId) {
        String currentUserId = getCurrentUserId();
        Conversation conversation = getConversation(conversationId);
        ensureGroupConversation(conversation);

        ConversationMember leavingMember = getConversationMember(conversationId, currentUserId);
        List<ConversationMember> members = conversationMemberRepository.findByIdConversationId(conversationId);

        List<String> memberIdsBeforeLeave = members.stream()
                .map(member -> member.getId().getUserId())
                .toList();

        if (members.size() <= 1) {
            deleteConversationData(conversation);
            publishConversationRemoved(conversationId, currentUserId, currentUserId, memberIdsBeforeLeave);
            return null;
        }

        if (leavingMember.getRole() == ConversationMemberRole.OWNER) {
            ConversationMember nextOwner = members.stream()
                    .filter(member -> !member.getId().getUserId().equals(currentUserId))
                    .min(Comparator.comparing(ConversationMember::getJoinedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_CONVERSATION_MEMBERS));

            nextOwner.setRole(ConversationMemberRole.OWNER);
            conversation.setCreatedBy(nextOwner.getId().getUserId());
            conversationMemberRepository.save(nextOwner);
            conversationRepository.save(conversation);
        }

        conversationMemberRepository.delete(leavingMember);
        List<String> remainingMemberIds = memberIdsBeforeLeave.stream()
                .filter(userId -> !userId.equals(currentUserId))
                .toList();
        ConversationResponse response = toConversationResponse(conversation);
        publishConversationUpsert(conversationId, currentUserId, currentUserId, remainingMemberIds);
        publishConversationRemoved(conversationId, currentUserId, currentUserId, List.of(currentUserId));
        return response;
    }

    @Transactional
    public void deleteConversationForCurrentUser(String conversationId) {
        String currentUserId = getCurrentUserId();
        getConversation(conversationId);
        ensureConversationMember(conversationId, currentUserId);

        LocalDateTime deletedAt = LocalDateTime.now();
        List<MessageDeletion> deletions = messageRepository.findVisibleMessagesByConversation(conversationId, currentUserId).stream()
                .map(message -> {
                    MessageDeletion deletion = new MessageDeletion();
                    deletion.setId(new MessageDeletionId(message.getId(), currentUserId));
                    deletion.setDeletedAt(deletedAt);
                    return deletion;
                })
                .toList();
        if (!deletions.isEmpty()) {
            messageDeletionRepository.saveAll(deletions);
        }

        ConversationDeletion deletion = new ConversationDeletion();
        deletion.setId(new ConversationDeletionId(conversationId, currentUserId));
        deletion.setDeletedAt(deletedAt);
        conversationDeletionRepository.save(deletion);

        publishConversationRemoved(conversationId, currentUserId, currentUserId, List.of(currentUserId));
    }

    private void deleteConversationData(Conversation conversation) {
        String conversationId = conversation.getId();
        List<String> messageIds = messageRepository.findByConversationId(conversationId).stream()
                .map(message -> message.getId())
                .toList();
        if (!messageIds.isEmpty()) {
            messageReceiptRepository.deleteByIdMessageIdIn(messageIds);
            messageDeletionRepository.deleteByIdMessageIdIn(messageIds);
            messageRepository.deleteByConversationId(conversationId);
        }
        conversationDeletionRepository.deleteByIdConversationId(conversationId);
        conversationMemberRepository.deleteByIdConversationId(conversationId);
        conversationRepository.delete(conversation);
    }

    @Transactional
    public ConversationResponse updateMemberNickname(String conversationId, String memberId, UpdateConversationNicknameRequest request) {
        String currentUserId = getCurrentUserId();
        Conversation conversation = getConversation(conversationId);
        ensureConversationMember(conversationId, currentUserId);

        ConversationMember targetMember = conversationMemberRepository.findById(new ConversationMemberId(conversationId, memberId))
                .orElseThrow(() -> new AppException(ErrorCode.NOT_CONVERSATION_MEMBER));

        String nickname = request.getNickname() == null ? null : request.getNickname().trim();
        targetMember.setNickname(nickname == null || nickname.isEmpty() ? null : nickname);
        conversationMemberRepository.save(targetMember);

        ConversationResponse response = toConversationResponse(conversation);
        publishConversationUpsert(conversationId, currentUserId, memberId, getConversationMemberIds(conversationId));
        return response;
    }

    @Transactional
    public ConversationResponse updateConversationEmoji(String conversationId, UpdateConversationEmojiRequest request) {
        String currentUserId = getCurrentUserId();
        Conversation conversation = getConversation(conversationId);
        ensureConversationMember(conversationId, currentUserId);

        String emoji = request.getEmoji() == null ? null : request.getEmoji().trim();
        if (!hasText(emoji)) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        conversation.setEmoji(emoji);
        ConversationResponse response = toConversationResponse(conversationRepository.save(conversation));
        publishConversationUpsert(conversationId, currentUserId, null, getConversationMemberIds(conversationId));
        return enrichConversationPreview(applyDirectFriendshipState(response, currentUserId), currentUserId);
    }

    @Transactional
    public ConversationResponse muteConversation(String conversationId, MuteConversationRequest request) {
        String currentUserId = getCurrentUserId();
        Conversation conversation = getConversation(conversationId);
        ConversationMember member = getConversationMember(conversationId, currentUserId);

        if (request.getMutedUntil().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        member.setMutedUntil(request.getMutedUntil());
        conversationMemberRepository.save(member);
        return enrichConversationPreview(toConversationResponse(conversation), currentUserId);
    }

    @Transactional
    public ConversationResponse unmuteConversation(String conversationId) {
        String currentUserId = getCurrentUserId();
        Conversation conversation = getConversation(conversationId);
        ConversationMember member = getConversationMember(conversationId, currentUserId);

        member.setMutedUntil(null);
        conversationMemberRepository.save(member);
        return enrichConversationPreview(toConversationResponse(conversation), currentUserId);
    }

    @Transactional
    public ConversationResponse updateGroupAvatar(String conversationId, MultipartFile file) {
        String currentUserId = getCurrentUserId();
        Conversation conversation = getConversation(conversationId);
        ensureGroupConversation(conversation);
        ensureCanManageGroup(conversationId, currentUserId);

        conversation.setAvatarUrl(r2StorageService.uploadConversationAvatar(conversationId, file));
        ConversationResponse response = toConversationResponse(conversationRepository.save(conversation));
        publishConversationUpsert(conversationId, currentUserId, null, getConversationMemberIds(conversationId));
        return response;
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversationPreviewForUser(String conversationId, String userId) {
        Conversation conversation = getConversation(conversationId);
        ensureConversationMember(conversationId, userId);
        return enrichConversationPreview(applyDirectFriendshipState(toConversationResponse(conversation), userId), userId);
    }

    @Transactional(readOnly = true)
    public List<String> getConversationMemberIds(String conversationId) {
        getConversation(conversationId);
        return conversationMemberRepository.findByIdConversationId(conversationId).stream()
                .map(member -> member.getId().getUserId())
                .toList();
    }

    @Transactional(readOnly = true)
    public ConversationInfoResponse getConversationInfo(String conversationId) {
        String currentUserId = getCurrentUserId();
        Conversation conversation = getConversation(conversationId);
        ensureConversationMember(conversationId, currentUserId);

        ConversationResponse conversationResponse = toConversationResponse(conversation);
        ConversationMemberResponse displayMember = null;

        if (conversation.getType() == ConversationType.DIRECT) {
            displayMember = conversationResponse.getMembers().stream()
                    .filter(member -> !member.getUserId().equals(currentUserId))
                    .findFirst()
                    .orElse(null);
        }

        String displayName = conversation.getType() == ConversationType.GROUP
                ? defaultIfBlank(conversation.getTitle(), "Nhóm chat")
                : getMemberDisplayName(displayMember);

        String avatarUrl = conversation.getType() == ConversationType.GROUP
                ? conversation.getAvatarUrl()
                : displayMember == null ? null : displayMember.getAvatarUrl();

        return ConversationInfoResponse.builder()
                .id(conversation.getId())
                .type(conversation.getType())
                .title(conversation.getTitle())
                .displayName(displayName)
                .avatarUrl(avatarUrl)
                .emoji(defaultIfBlank(conversation.getEmoji(), "👍"))
                .mutedUntil(getMutedUntil(conversationId, currentUserId))
                .status(conversation.getType() == ConversationType.GROUP ? "Nhóm chat" : "Ngoại tuyến")
                .createdBy(conversation.getCreatedBy())
                .createdAt(conversation.getCreatedAt())
                .memberCount(conversationResponse.getMembers().size())
                .members(conversationResponse.getMembers())
                .stats(buildConversationInfoStats(conversationId, conversationResponse.getMembers().size()))
                .settings(List.of("Chỉnh sửa biệt danh", "Thay đổi biểu tượng cảm xúc"))
                .build();
    }

    private ConversationResponse createDirectConversation(String currentUserId, CreateConversationRequest request) {
        List<String> memberIds = sanitizeMemberIds(request.getMemberIds());
        if (memberIds.size() != 1 || currentUserId.equals(memberIds.getFirst())) {
            throw new AppException(ErrorCode.INVALID_DIRECT_CONVERSATION_MEMBERS);
        }

        String targetUserId = memberIds.getFirst();
        getUser(targetUserId);
        ensureAreFriends(currentUserId, targetUserId);

        Conversation existingConversation = findExistingDirectConversation(currentUserId, targetUserId);
        if (existingConversation != null) {
            return applyDirectFriendshipState(toConversationResponse(existingConversation), currentUserId);
        }

        return applyDirectFriendshipState(createDirectConversation(currentUserId, targetUserId), currentUserId);
    }

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
        ensureAllAreFriends(currentUserId, allMemberIds.stream()
                .filter(memberId -> !memberId.equals(currentUserId))
                .toList());

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

    @Transactional
    public ConversationResponse findOrCreateDirectConversation(String firstUserId, String secondUserId) {
        if (firstUserId == null || secondUserId == null || firstUserId.equals(secondUserId)) {
            throw new AppException(ErrorCode.INVALID_DIRECT_CONVERSATION_MEMBERS);
        }

        getUser(firstUserId);
        getUser(secondUserId);

        Conversation existingConversation = findExistingDirectConversation(firstUserId, secondUserId);
        if (existingConversation != null) {
            return toConversationResponse(existingConversation);
        }

        return createDirectConversation(firstUserId, secondUserId);
    }

    private ConversationResponse createDirectConversation(String ownerId, String memberId) {
        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.DIRECT);
        conversation.setCreatedBy(ownerId);
        conversation.setTitle(null);
        conversation.setAvatarUrl(null);
        Conversation savedConversation = conversationRepository.save(conversation);

        List<ConversationMember> members = List.of(
                buildConversationMember(savedConversation.getId(), ownerId, ConversationMemberRole.OWNER),
                buildConversationMember(savedConversation.getId(), memberId, ConversationMemberRole.MEMBER)
        );
        conversationMemberRepository.saveAll(members);

        return toConversationResponse(savedConversation);
    }

    private ConversationResponse toConversationResponse(Conversation conversation) {
        return toConversationResponse(conversation, null);
    }

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
                            .nickname(member.getNickname())
                            .avatarUrl(user.getAvatarUrl())
                            .role(member.getRole())
                            .joinedAt(member.getJoinedAt())
                            .online(presenceService.isOnline(user.getId()))
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
                .emoji(defaultIfBlank(conversation.getEmoji(), "👍"))
                .members(memberResponses)
                .build();
    }

    private ConversationResponse enrichConversationPreview(ConversationResponse response, String userId) {
        response.setUnreadCount(messageRepository.countUnreadMessages(response.getId(), userId));
        response.setLatestMessage(getLatestVisibleMessage(response.getId(), userId));
        response.setMutedUntil(getMutedUntil(response.getId(), userId));
        return response;
    }

    private LocalDateTime getMutedUntil(String conversationId, String userId) {
        return conversationMemberRepository.findById(new ConversationMemberId(conversationId, userId))
                .map(ConversationMember::getMutedUntil)
                .orElse(null);
    }

    private ConversationResponse applyDirectFriendshipState(ConversationResponse response, String currentUserId) {
        if (response.getType() != ConversationType.DIRECT || response.getMembers() == null) {
            response.setFriendshipStatus("NONE");
            response.setFriendshipDirection("NONE");
            return response;
        }

        String otherUserId = response.getMembers().stream()
                .map(ConversationMemberResponse::getUserId)
                .filter(memberId -> !memberId.equals(currentUserId))
                .findFirst()
                .orElse(null);

        if (otherUserId == null) {
            response.setFriendshipStatus("NONE");
            response.setFriendshipDirection("NONE");
            return response;
        }

        RelationshipService.RelationshipState relationship = relationshipService.resolve(currentUserId, otherUserId);
        response.setFriendshipStatus(relationship.status());
        response.setFriendshipDirection(relationship.direction());
        response.setBlockedByCurrentUser(relationship.blockedByCurrentUser());
        response.setCurrentUserBlocked(relationship.currentUserBlocked());

        return response;
    }

    private MessageResponse getLatestVisibleMessage(String conversationId, String userId) {
        var page = messageRepository.findVisibleMessagesByConversationPaged(conversationId, userId, PageRequest.of(0, 1));
        if (page.isEmpty()) {
            return null;
        }

        return toMessageResponse(page.getContent().getFirst(), userId);
    }

    private MessageResponse toMessageResponse(Message message, String userId) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setConversationId(message.getConversationId());
        response.setSenderId(message.getSenderId());
        response.setType(message.getType());
        response.setContent(message.getContent());
        response.setCreatedAt(message.getCreatedAt());
        response.setEdited(message.isEdited());
        response.setEditedAt(message.getEditedAt());
        response.setRecalled(message.isRecalled());
        response.setRecalledAt(message.getRecalledAt());
        response.setRecalledBy(message.getRecalledBy());
        response.setRead(messageReceiptRepository.existsById(new MessageReceiptId(message.getId(), userId)));
        response.setAttachment(attachmentRepository.findByMessageId(message.getId())
                .map(this::toAttachmentResponse)
                .orElse(null));
        return response;
    }

    private AttachmentResponse toAttachmentResponse(Attachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .fileUrl(attachment.getFileUrl())
                .fileName(attachment.getFileName())
                .mimeType(attachment.getMimeType())
                .fileSize(attachment.getFileSize())
                .build();
    }

    private Conversation getConversation(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
    }

    private void ensureGroupConversation(Conversation conversation) {
        if (conversation.getType() != ConversationType.GROUP) {
            throw new AppException(ErrorCode.GROUP_OPERATION_NOT_ALLOWED);
        }
    }

    private void ensureCanManageGroup(String conversationId, String userId) {
        ConversationMember member = getConversationMember(conversationId, userId);
        if (member.getRole() != ConversationMemberRole.OWNER) {
            throw new AppException(ErrorCode.NOT_GROUP_MANAGER);
        }
    }

    private ConversationMember getConversationMember(String conversationId, String userId) {
        return conversationMemberRepository.findById(new ConversationMemberId(conversationId, userId))
                .orElseThrow(() -> new AppException(ErrorCode.NOT_CONVERSATION_MEMBER));
    }

    private void ensureConversationMember(String conversationId, String userId) {
        if (!conversationMemberRepository.existsByIdConversationIdAndIdUserId(conversationId, userId)) {
            throw new AppException(ErrorCode.NOT_CONVERSATION_MEMBER);
        }
    }

    private List<ConversationInfoStatResponse> buildConversationInfoStats(String conversationId, int memberCount) {
        long links = messageRepository.countByConversationIdAndTypeAndContentContainingIgnoreCase(
                conversationId,
                MessageType.TEXT,
                "http://"
        ) + messageRepository.countByConversationIdAndTypeAndContentContainingIgnoreCase(
                conversationId,
                MessageType.TEXT,
                "https://"
        );
        long files = messageRepository.countByConversationIdAndType(conversationId, MessageType.FILE);
        long images = messageRepository.countByConversationIdAndType(conversationId, MessageType.IMAGE);

        return List.of(
                ConversationInfoStatResponse.builder()
                        .id("members")
                        .label("Thành viên")
                        .value(String.valueOf(memberCount))
                        .build(),
                ConversationInfoStatResponse.builder()
                        .id("links")
                        .label("Link")
                        .value(String.valueOf(links))
                        .build(),
                ConversationInfoStatResponse.builder()
                        .id("files")
                        .label("File")
                        .value(String.valueOf(files))
                        .build(),
                ConversationInfoStatResponse.builder()
                        .id("images")
                        .label("Hình ảnh")
                        .value(String.valueOf(images))
                        .build()
        );
    }

    private String getMemberDisplayName(ConversationMemberResponse member) {
        if (member == null) {
            return "Người dùng";
        }

        if (hasText(member.getNickname())) {
            return member.getNickname();
        }
        if (hasText(member.getDisplayName())) {
            return member.getDisplayName();
        }
        if (hasText(member.getUsername())) {
            return member.getUsername();
        }
        return "Người dùng";
    }

    private String defaultIfBlank(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private ConversationMember buildConversationMember(String conversationId, String userId, ConversationMemberRole role) {
        ConversationMember member = new ConversationMember();
        member.setId(new ConversationMemberId(conversationId, userId));
        member.setRole(role);
        return member;
    }

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

    private Map<String, User> getUsersByIds(List<String> userIds) {
        List<User> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        return users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private User getUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private void ensureAllAreFriends(String currentUserId, List<String> memberIds) {
        memberIds.forEach(memberId -> ensureAreFriends(currentUserId, memberId));
    }

    private void ensureAreFriends(String firstUserId, String secondUserId) {
        relationshipService.ensureAcceptedFriendship(firstUserId, secondUserId);
    }

    private String getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getMyConversations() {
        String userId = getCurrentUserId();

        List<ConversationMember> conversationMembers = conversationMemberRepository.findByIdUserId(userId);

        if (conversationMembers.isEmpty()) {
            return List.of();
        }

        List<String> conversationIds = conversationMembers.stream()
                .map(member -> member.getId().getConversationId())
                .distinct()
                .toList();

        return conversationRepository.findAllById(conversationIds).stream()
                .map(this::toConversationResponse)
                .map(response -> applyDirectFriendshipState(response, userId))
                .map(response -> enrichConversationPreview(response, userId))
                .filter(response -> shouldShowConversation(response, userId))
                .sorted(Comparator.comparing(this::getConversationPreviewTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private boolean shouldShowConversation(ConversationResponse response, String userId) {
        if (response.getLatestMessage() != null) {
            return true;
        }
        return !conversationDeletionRepository.existsById(new ConversationDeletionId(response.getId(), userId));
    }

    private LocalDateTime getConversationPreviewTime(ConversationResponse response) {
        if (response.getLatestMessage() != null && response.getLatestMessage().getCreatedAt() != null) {
            return response.getLatestMessage().getCreatedAt();
        }
        return response.getCreatedAt();
    }

    private void publishConversationUpsertToMembers(String conversationId, String actorUserId) {
        publishConversationUpsert(conversationId, actorUserId, null, getConversationMemberIds(conversationId));
    }

    private void publishConversationUpsert(
            String conversationId,
            String actorUserId,
            String targetUserId,
            List<String> recipientIds) {
        recipientIds.stream()
                .distinct()
                .forEach(recipientId -> {
                    ConversationResponse preview = getConversationPreviewForUser(conversationId, recipientId);
                    messagingTemplate.convertAndSendToUser(
                            recipientId,
                            CONVERSATION_QUEUE,
                            buildConversationEvent(EVENT_UPSERT, conversationId, actorUserId, targetUserId, preview)
                    );
                });
    }

    private void publishConversationRemoved(
            String conversationId,
            String actorUserId,
            String targetUserId,
            List<String> recipientIds) {
        ConversationRealtimeEventResponse event = buildConversationEvent(
                EVENT_REMOVED,
                conversationId,
                actorUserId,
                targetUserId,
                null
        );
        recipientIds.stream()
                .distinct()
                .forEach(recipientId -> messagingTemplate.convertAndSendToUser(recipientId, CONVERSATION_QUEUE, event));
    }

    private ConversationRealtimeEventResponse buildConversationEvent(
            String eventType,
            String conversationId,
            String actorUserId,
            String targetUserId,
            ConversationResponse conversation) {
        return ConversationRealtimeEventResponse.builder()
                .eventType(eventType)
                .conversationId(conversationId)
                .actorUserId(actorUserId)
                .targetUserId(targetUserId)
                .conversation(conversation)
                .occurredAt(LocalDateTime.now())
                .build();
    }
}
