package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.request.ChangePasswordRequest;
import com.ncbachhhh.LTUDM.dto.request.UserRegisterRequest;
import com.ncbachhhh.LTUDM.dto.request.UserUpdateRequest;
import com.ncbachhhh.LTUDM.dto.response.UserProfileResponse;
import com.ncbachhhh.LTUDM.dto.response.UserResponse;
import com.ncbachhhh.LTUDM.entity.Friendship.Friendship;
import com.ncbachhhh.LTUDM.entity.Friendship.FriendshipStatus;
import com.ncbachhhh.LTUDM.entity.User.User;
import com.ncbachhhh.LTUDM.entity.User.UserRole;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.mapper.UserMapper;
import com.ncbachhhh.LTUDM.repository.FriendshipRepository;
import com.ncbachhhh.LTUDM.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    FriendshipRepository friendshipRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    R2StorageService r2StorageService;

    public UserResponse createUser(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        User user = userMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        if (user.getRole() == null) {
            user.setRole(UserRole.USER);
        }
        user.setActive(true);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse getMyInfo() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return userMapper.toUserResponse(userRepository.findById(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    public void changePassword(ChangePasswordRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        User user = userRepository.findById(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOld_password(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.WRONG_OLD_PASSWORD);
        }
        if (!request.getNew_password().equals(request.getConfirm_password())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        if (passwordEncoder.matches(request.getNew_password(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.SAME_PASSWORD);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNew_password()));
        userRepository.save(user);
    }

    public UserResponse getUserById(String userId) {
        return userMapper.toUserResponse(userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    public List<UserProfileResponse> searchUsers(String query) {
        String currentUserId = getCurrentUserId();
        String normalizedQuery = normalizeSearchQuery(query);

        return userRepository.searchOtherActiveUsers(currentUserId, normalizedQuery, PageRequest.of(0, 20)).stream()
                .map(user -> toUserProfileResponse(user, currentUserId))
                .toList();
    }

    public UserProfileResponse getUserProfile(String userId) {
        String currentUserId = getCurrentUserId();
        if (!StringUtils.hasText(userId) || currentUserId.equals(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!user.isActive()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        return toUserProfileResponse(user, currentUserId);
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateUser(request, user);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse updateMyAvatar(MultipartFile file) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        User user = userRepository.findById(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setAvatarUrl(r2StorageService.uploadAvatar(user.getId(), file));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void banUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setActive(false);
        userRepository.save(user);
    }

    public void unbanUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setActive(true);
        userRepository.save(user);
    }

    private String getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }

    private String normalizeSearchQuery(String query) {
        if (!StringUtils.hasText(query) || query.trim().length() < 2) {
            throw new AppException(ErrorCode.SEARCH_QUERY_REQUIRED);
        }
        return query.trim();
    }

    private UserProfileResponse toUserProfileResponse(User user, String currentUserId) {
        Friendship friendship = friendshipRepository.findBetweenUsers(currentUserId, user.getId()).orElse(null);
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .friendshipStatus(resolveFriendshipStatus(friendship, currentUserId))
                .friendshipDirection(resolveFriendshipDirection(friendship, currentUserId))
                .build();
    }

    private String resolveFriendshipStatus(Friendship friendship, String currentUserId) {
        if (friendship == null) {
            return "NONE";
        }
        return friendship.getStatus().name();
    }

    private String resolveFriendshipDirection(Friendship friendship, String currentUserId) {
        if (friendship == null || friendship.getStatus() != FriendshipStatus.PENDING) {
            return "NONE";
        }
        return friendship.getRequesterId().equals(currentUserId) ? "OUTGOING" : "INCOMING";
    }
}
