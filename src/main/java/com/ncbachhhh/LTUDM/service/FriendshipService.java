package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.response.ConversationResponse;
import com.ncbachhhh.LTUDM.dto.response.FriendshipResponse;
import com.ncbachhhh.LTUDM.dto.response.UserProfileResponse;
import com.ncbachhhh.LTUDM.entity.Friendship.Friendship;
import com.ncbachhhh.LTUDM.entity.Friendship.FriendshipStatus;
import com.ncbachhhh.LTUDM.entity.User.User;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.repository.FriendshipRepository;
import com.ncbachhhh.LTUDM.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendshipService {
    FriendshipRepository friendshipRepository;
    UserRepository userRepository;
    ConversationService conversationService;

    @Transactional
    public FriendshipResponse sendRequest(String addresseeId) {
        String currentUserId = getCurrentUserId();
        validateTargetUser(currentUserId, addresseeId);

        Friendship existing = friendshipRepository.findBetweenUsers(currentUserId, addresseeId).orElse(null);
        if (existing != null) {
            if (existing.getStatus() == FriendshipStatus.DECLINED) {
                existing.setRequesterId(currentUserId);
                existing.setAddresseeId(addresseeId);
                existing.setStatus(FriendshipStatus.PENDING);
                return toResponse(friendshipRepository.save(existing), currentUserId, null);
            }
            throw new AppException(ErrorCode.FRIENDSHIP_ALREADY_EXISTS);
        }

        Friendship friendship = new Friendship();
        friendship.setRequesterId(currentUserId);
        friendship.setAddresseeId(addresseeId);
        friendship.setStatus(FriendshipStatus.PENDING);

        return toResponse(friendshipRepository.save(friendship), currentUserId, null);
    }

    @Transactional
    public FriendshipResponse acceptRequest(String friendshipId) {
        String currentUserId = getCurrentUserId();
        Friendship friendship = getFriendship(friendshipId);
        ensureReceivedPendingRequest(friendship, currentUserId);

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        Friendship savedFriendship = friendshipRepository.save(friendship);
        ConversationResponse conversation = conversationService.findOrCreateDirectConversation(
                savedFriendship.getRequesterId(),
                savedFriendship.getAddresseeId());

        return toResponse(savedFriendship, currentUserId, conversation);
    }

    @Transactional
    public FriendshipResponse declineRequest(String friendshipId) {
        String currentUserId = getCurrentUserId();
        Friendship friendship = getFriendship(friendshipId);
        ensureReceivedPendingRequest(friendship, currentUserId);

        friendship.setStatus(FriendshipStatus.DECLINED);
        return toResponse(friendshipRepository.save(friendship), currentUserId, null);
    }

    public List<FriendshipResponse> getIncomingRequests() {
        String currentUserId = getCurrentUserId();
        return friendshipRepository.findByAddresseeId(currentUserId).stream()
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.PENDING)
                .map(friendship -> toResponse(friendship, currentUserId, null))
                .toList();
    }

    public List<FriendshipResponse> getOutgoingRequests() {
        String currentUserId = getCurrentUserId();
        return friendshipRepository.findByRequesterId(currentUserId).stream()
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.PENDING)
                .map(friendship -> toResponse(friendship, currentUserId, null))
                .toList();
    }

    public List<FriendshipResponse> getFriends() {
        String currentUserId = getCurrentUserId();
        return friendshipRepository.findByUserIdAndStatus(currentUserId, FriendshipStatus.ACCEPTED).stream()
                .map(friendship -> toResponse(friendship, currentUserId, null))
                .toList();
    }

    private Friendship getFriendship(String friendshipId) {
        if (!StringUtils.hasText(friendshipId)) {
            throw new AppException(ErrorCode.FRIENDSHIP_NOT_FOUND);
        }
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new AppException(ErrorCode.FRIENDSHIP_NOT_FOUND));
    }

    private void validateTargetUser(String currentUserId, String targetUserId) {
        if (!StringUtils.hasText(targetUserId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        if (currentUserId.equals(targetUserId)) {
            throw new AppException(ErrorCode.CANNOT_FRIEND_SELF);
        }
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!targetUser.isActive()) {
            throw new AppException(ErrorCode.USER_BANNED);
        }
    }

    private void ensureReceivedPendingRequest(Friendship friendship, String currentUserId) {
        if (!friendship.getAddresseeId().equals(currentUserId)) {
            throw new AppException(ErrorCode.FRIENDSHIP_REQUEST_NOT_RECEIVED);
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new AppException(ErrorCode.FRIENDSHIP_REQUEST_NOT_PENDING);
        }
    }

    private FriendshipResponse toResponse(Friendship friendship, String currentUserId, ConversationResponse conversation) {
        String otherUserId = friendship.getRequesterId().equals(currentUserId)
                ? friendship.getAddresseeId()
                : friendship.getRequesterId();
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return FriendshipResponse.builder()
                .id(friendship.getId())
                .requesterId(friendship.getRequesterId())
                .addresseeId(friendship.getAddresseeId())
                .status(friendship.getStatus())
                .createdAt(friendship.getCreatedAt())
                .updatedAt(friendship.getUpdatedAt())
                .user(toUserProfile(otherUser, friendship, currentUserId))
                .conversation(conversation)
                .build();
    }

    private UserProfileResponse toUserProfile(User user, Friendship friendship, String currentUserId) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .friendshipStatus(friendship.getStatus().name())
                .friendshipDirection(resolveFriendshipDirection(friendship, currentUserId))
                .build();
    }

    private String resolveFriendshipDirection(Friendship friendship, String currentUserId) {
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            return "NONE";
        }
        return friendship.getRequesterId().equals(currentUserId) ? "OUTGOING" : "INCOMING";
    }

    private String getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }
}
