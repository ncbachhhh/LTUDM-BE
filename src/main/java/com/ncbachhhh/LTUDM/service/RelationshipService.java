package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.entity.Friendship.Friendship;
import com.ncbachhhh.LTUDM.entity.Friendship.FriendshipStatus;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.repository.BlockRepository;
import com.ncbachhhh.LTUDM.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RelationshipService {
    private static final String NONE = "NONE";
    private static final String OUTGOING = "OUTGOING";
    private static final String INCOMING = "INCOMING";

    private final FriendshipRepository friendshipRepository;
    private final BlockRepository blockRepository;

    // Resolve trạng thái quan hệ giữa current user và user khác, ưu tiên bảng blocks trước friendship.
    public RelationshipState resolve(String currentUserId, String otherUserId) {
        if (!StringUtils.hasText(currentUserId) || !StringUtils.hasText(otherUserId)) {
            return RelationshipState.none();
        }

        // Block mới nằm trong bảng blocks được ưu tiên cao nhất vì nó ảnh hưởng trực tiếp đến chat/profile.
        boolean blockedByCurrentUser = blockRepository.existsByBlockerIdAndBlockedId(currentUserId, otherUserId);
        boolean currentUserBlocked = blockRepository.existsByBlockerIdAndBlockedId(otherUserId, currentUserId);
        if (blockedByCurrentUser || currentUserBlocked) {
            return new RelationshipState(
                    FriendshipStatus.BLOCKED.name(),
                    blockedByCurrentUser ? OUTGOING : INCOMING,
                    blockedByCurrentUser,
                    currentUserBlocked
            );
        }

        Friendship friendship = findRelationship(currentUserId, otherUserId);
        if (friendship == null) {
            return RelationshipState.none();
        }

        // Direction cho FE biet request/block là current user tạo ra hay nhận vào.
        String direction = switch (friendship.getStatus()) {
            case PENDING -> friendship.getRequesterId().equals(currentUserId) ? OUTGOING : INCOMING;
            case BLOCKED -> friendship.getRequesterId().equals(currentUserId) ? OUTGOING : INCOMING;
            default -> NONE;
        };

        boolean blockedFromLegacyFriendship = friendship.getStatus() == FriendshipStatus.BLOCKED;
        boolean blockedByRequester = blockedFromLegacyFriendship && friendship.getRequesterId().equals(currentUserId);

        return new RelationshipState(
                friendship.getStatus().name(),
                direction,
                blockedByRequester,
                blockedFromLegacyFriendship && !blockedByRequester
        );
    }

    // Guard dùng ở conversation/message để đảm bảo hai user đang là bạn bè accepted.
    public void ensureAcceptedFriendship(String currentUserId, String otherUserId) {
        RelationshipState relationship = resolve(currentUserId, otherUserId);
        if (!FriendshipStatus.ACCEPTED.name().equals(relationship.status())) {
            throw new AppException(ErrorCode.NOT_FRIENDS);
        }
    }

    // Tìm relationship cũ, ưu tiên legacy BLOCKED nếu dữ liệu cũ vẫn còn trong friendships.
    private Friendship findRelationship(String currentUserId, String otherUserId) {
        return friendshipRepository.findAllBetweenUsers(currentUserId, otherUserId).stream()
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.BLOCKED)
                .findFirst()
                .or(() -> friendshipRepository.findBetweenUsers(currentUserId, otherUserId))
                .orElse(null);
    }

    public record RelationshipState(
            String status,
            String direction,
            boolean blockedByCurrentUser,
            boolean currentUserBlocked
    ) {
        // Trạng thái mặc định khi không có block/friendship giữa hai user.
        private static RelationshipState none() {
            return new RelationshipState(NONE, NONE, false, false);
        }
    }
}
