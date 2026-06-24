package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.response.ConversationResponse;
import com.ncbachhhh.LTUDM.dto.response.FriendshipResponse;
import com.ncbachhhh.LTUDM.dto.response.UserProfileResponse;
import com.ncbachhhh.LTUDM.entity.Block.Block;
import com.ncbachhhh.LTUDM.entity.Friendship.Friendship;
import com.ncbachhhh.LTUDM.entity.Friendship.FriendshipStatus;
import com.ncbachhhh.LTUDM.entity.Key.BlockId;
import com.ncbachhhh.LTUDM.entity.User.User;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.repository.BlockRepository;
import com.ncbachhhh.LTUDM.repository.FriendshipRepository;
import com.ncbachhhh.LTUDM.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
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
    BlockRepository blockRepository;
    UserRepository userRepository;
    ConversationService conversationService;
    PresenceService presenceService;
    RelationshipService relationshipService;

    // Gửi lời mời kết bạn, reuse record DECLINED cũ nếu có để tránh tạo duplicate relationship.
    @Transactional
    public FriendshipResponse sendRequest(String addresseeId) {
        String currentUserId = getCurrentUserId();
        validateTargetUser(currentUserId, addresseeId);

        Friendship existing = friendshipRepository.findBetweenUsers(currentUserId, addresseeId).orElse(null);
        if (existing != null) {
            // Lời mời đã bị decline có thể được gửi lai bằng cách đổi requester/addressee và set PENDING.
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

    // Chấp nhận request và đảm bảo hai user có direct conversation để chat với nhau.
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

    // Tu chồi request đang pending mà current user là người nhận.
    @Transactional
    public FriendshipResponse declineRequest(String friendshipId) {
        String currentUserId = getCurrentUserId();
        Friendship friendship = getFriendship(friendshipId);
        ensureReceivedPendingRequest(friendship, currentUserId);

        friendship.setStatus(FriendshipStatus.DECLINED);
        return toResponse(friendshipRepository.save(friendship), currentUserId, null);
    }

    // Thu hồi request pending do current user đã gửi.
    @Transactional
    public void withdrawRequest(String friendshipId) {
        String currentUserId = getCurrentUserId();
        Friendship friendship = getFriendship(friendshipId);

        if (!friendship.getRequesterId().equals(currentUserId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new AppException(ErrorCode.FRIENDSHIP_REQUEST_NOT_PENDING);
        }

        friendshipRepository.delete(friendship);
    }

    // Xóa bạn bè hoặc legacy blocked friendship nếu current user có quyền.
    @Transactional
    public void deleteFriend(String friendshipId) {
        String currentUserId = getCurrentUserId();
        Friendship friendship = getFriendship(friendshipId);

        ensureFriendshipParticipant(friendship, currentUserId);
        if (friendship.getStatus() != FriendshipStatus.ACCEPTED
                && friendship.getStatus() != FriendshipStatus.BLOCKED) {
            throw new AppException(ErrorCode.NOT_FRIENDS);
        }
        if (friendship.getStatus() == FriendshipStatus.BLOCKED
                && !friendship.getRequesterId().equals(currentUserId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        friendshipRepository.delete(friendship);
    }

    // Chặn user bằng bảng blocks mới; relationship resolver sẽ ưu tiên bằng nay.
    @Transactional
    public FriendshipResponse blockUser(String userId) {
        String currentUserId = getCurrentUserId();
        validateTargetUser(currentUserId, userId);

        BlockId blockId = new BlockId(currentUserId, userId);
        if (!blockRepository.existsById(blockId)) {
            Block block = new Block();
            block.setId(blockId);
            blockRepository.save(block);
        }

        Friendship friendship = friendshipRepository.findBetweenUsers(currentUserId, userId).orElse(null);
        // Nếu dữ liệu cũ có friendship BLOCKED, restore thành ACCEPTED để trạng thái block chỉ do bảng blocks quyết định.
        if (friendship != null && friendship.getStatus() == FriendshipStatus.BLOCKED
                && friendship.getRequesterId().equals(currentUserId)) {
            friendship.setStatus(FriendshipStatus.ACCEPTED);
            friendship = friendshipRepository.save(friendship);
        }

        return toBlockedResponse(currentUserId, userId, friendship);
    }

    // Bỏ chặn user trong bảng blocks và xử lý legacy blocked friendship nếu còn tồn tại.
    @Transactional
    public void unblockUser(String userId) {
        String currentUserId = getCurrentUserId();
        validateUnblockTarget(currentUserId, userId);

        boolean existsInBlocks = blockRepository.existsByBlockerIdAndBlockedId(currentUserId, userId);
        Friendship legacyBlockedFriendship = friendshipRepository.findAllBetweenUsers(currentUserId, userId).stream()
                .filter(friendship -> friendship.getRequesterId().equals(currentUserId))
                .filter(friendship -> friendship.getAddresseeId().equals(userId))
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.BLOCKED)
                .findFirst()
                .orElse(null);

        if (!existsInBlocks && legacyBlockedFriendship == null) {
            throw new AppException(ErrorCode.USER_NOT_BLOCKED);
        }

        if (existsInBlocks) {
            blockRepository.deleteByBlockerIdAndBlockedId(currentUserId, userId);
        }
        if (legacyBlockedFriendship != null) {
            legacyBlockedFriendship.setStatus(FriendshipStatus.ACCEPTED);
            friendshipRepository.save(legacyBlockedFriendship);
        }
    }

    // Lấy danh sách user bị current user chặn, gồm cả records mới trong blocks và legacy friendship BLOCKED.
    public List<FriendshipResponse> getBlockedUsers() {
        String currentUserId = getCurrentUserId();
        List<FriendshipResponse> blockedByBlockTable = blockRepository.findByBlockerId(currentUserId).stream()
                .map(block -> {
                    String blockedUserId = block.getId().getBlockedId();
                    Friendship friendship = friendshipRepository.findBetweenUsers(currentUserId, blockedUserId).orElse(null);
                    return toBlockedResponse(currentUserId, blockedUserId, friendship);
                })
                .toList();

        List<FriendshipResponse> legacyBlocked = friendshipRepository.findByRequesterIdAndStatus(currentUserId, FriendshipStatus.BLOCKED).stream()
                .filter(friendship -> blockedByBlockTable.stream()
                        .noneMatch(response -> response.getUser() != null
                                && response.getUser().getId().equals(friendship.getAddresseeId())))
                .map(friendship -> toBlockedResponse(currentUserId, friendship.getAddresseeId(), friendship))
                .toList();

        return java.util.stream.Stream.concat(blockedByBlockTable.stream(), legacyBlocked.stream())
                .toList();
    }

    // Lấy request đang pending mà current user là addressee.
    public List<FriendshipResponse> getIncomingRequests() {
        String currentUserId = getCurrentUserId();
        return friendshipRepository.findByAddresseeId(currentUserId).stream()
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.PENDING)
                .map(friendship -> toResponse(friendship, currentUserId, null))
                .toList();
    }

    // Lấy request đang pending mà current user là requester.
    public List<FriendshipResponse> getOutgoingRequests() {
        String currentUserId = getCurrentUserId();
        return friendshipRepository.findByRequesterId(currentUserId).stream()
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.PENDING)
                .map(friendship -> toResponse(friendship, currentUserId, null))
                .toList();
    }

    // Lấy danh sách friendship ACCEPTED của current user.
    public List<FriendshipResponse> getFriends() {
        String currentUserId = getCurrentUserId();
        return friendshipRepository.findByUserIdAndStatus(currentUserId, FriendshipStatus.ACCEPTED).stream()
                .map(friendship -> toResponse(friendship, currentUserId, null))
                .toList();
    }

    // Tìm trong danh sách bạn bè theo tên để FE hiển thị khi tạo nhom/tìm ban.
    public List<UserProfileResponse> searchMyFriendsByName(String name) {
        String currentUserId = getCurrentUserId();
        String normalizedName = normalizeName(name);

        return userRepository.searchAcceptedFriendsByName(currentUserId, normalizedName, PageRequest.of(0, 20)).stream()
                .map(user -> {
                    Friendship friendship = friendshipRepository.findBetweenUsers(currentUserId, user.getId())
                            .orElseThrow(() -> new AppException(ErrorCode.FRIENDSHIP_NOT_FOUND));
                    return toUserProfile(user, currentUserId);
                })
                .toList();
    }

    // Load friendship theo id và chuẩn hóa lỗi khi id rỗng/không tồn tại.
    private Friendship getFriendship(String friendshipId) {
        if (!StringUtils.hasText(friendshipId)) {
            throw new AppException(ErrorCode.FRIENDSHIP_NOT_FOUND);
        }
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new AppException(ErrorCode.FRIENDSHIP_NOT_FOUND));
    }

    // Validate user đích cho send/block: tồn tại, active và không phải chính mình.
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

    // Validate riêng cho unblock vì user bị block có thể không cần load full entity active như send request.
    private void validateUnblockTarget(String currentUserId, String targetUserId) {
        if (!StringUtils.hasText(targetUserId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        if (currentUserId.equals(targetUserId)) {
            throw new AppException(ErrorCode.CANNOT_FRIEND_SELF);
        }
    }

    // Chuẩn hóa query search bạn bè và bắt tối thiểu 2 ký tự.
    private String normalizeName(String name) {
        if (!StringUtils.hasText(name) || name.trim().length() < 2) {
            throw new AppException(ErrorCode.SEARCH_QUERY_REQUIRED);
        }
        return name.trim();
    }

    // Đảm bảo current user là người nhận của request pending.
    private void ensureReceivedPendingRequest(Friendship friendship, String currentUserId) {
        if (!friendship.getAddresseeId().equals(currentUserId)) {
            throw new AppException(ErrorCode.FRIENDSHIP_REQUEST_NOT_RECEIVED);
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new AppException(ErrorCode.FRIENDSHIP_REQUEST_NOT_PENDING);
        }
    }

    // Đảm bảo current user là một trong hai đầu của friendship.
    private void ensureFriendshipParticipant(Friendship friendship, String currentUserId) {
        if (!friendship.getRequesterId().equals(currentUserId)
                && !friendship.getAddresseeId().equals(currentUserId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
    }

    // Build response friendship kèm profile của người còn lại và conversation nếu có.
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
                .user(toUserProfile(otherUser, currentUserId))
                .conversation(conversation)
                .build();
    }

    // Build response cho block state, vì block mới có thể không có friendship id.
    private FriendshipResponse toBlockedResponse(String currentUserId, String blockedUserId, Friendship friendship) {
        User blockedUser = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return FriendshipResponse.builder()
                .id(friendship == null ? null : friendship.getId())
                .requesterId(currentUserId)
                .addresseeId(blockedUserId)
                .status(FriendshipStatus.BLOCKED)
                .createdAt(friendship == null ? null : friendship.getCreatedAt())
                .updatedAt(friendship == null ? null : friendship.getUpdatedAt())
                .user(toUserProfile(blockedUser, currentUserId))
                .conversation(null)
                .build();
    }

    // Map user thành profile nhỏ, kèm relationship/presence để FE hiện nút hành động đúng.
    private UserProfileResponse toUserProfile(User user, String currentUserId) {
        RelationshipService.RelationshipState relationship = relationshipService.resolve(currentUserId, user.getId());

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .backgroundUrl(user.getBackgroundUrl())
                .friendshipStatus(relationship.status())
                .friendshipDirection(relationship.direction())
                .online(presenceService.isOnline(user.getId()))
                .build();
    }

    // Lấy user id hiện tại từ SecurityContext.
    private String getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }
}
