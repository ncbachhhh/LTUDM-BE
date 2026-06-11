package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.request.ChangePasswordRequest;
import com.ncbachhhh.LTUDM.dto.request.UserRegisterRequest;
import com.ncbachhhh.LTUDM.dto.request.UserProfileUpdateRequest;
import com.ncbachhhh.LTUDM.dto.request.UserSettingsUpdateRequest;
import com.ncbachhhh.LTUDM.dto.request.UserUpdateRequest;
import com.ncbachhhh.LTUDM.dto.response.UserProfileResponse;
import com.ncbachhhh.LTUDM.dto.response.UserResponse;
import com.ncbachhhh.LTUDM.entity.User.User;
import com.ncbachhhh.LTUDM.entity.User.UserRole;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.mapper.UserMapper;
import com.ncbachhhh.LTUDM.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    private static final int GENERATED_USERNAME_MAX_LENGTH = 20;
    private static final Pattern USERNAME_SAFE_CHARS = Pattern.compile("[^a-z0-9_]");

    UserRepository userRepository;
    PresenceService presenceService;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    R2StorageService r2StorageService;
    RelationshipService relationshipService;

    public UserResponse createUser(UserRegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String username = resolveRegistrationUsername(request.getUsername(), email);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        User user = userMapper.toUser(request);
        user.setEmail(email);
        user.setUsername(username);
        user.setDisplayName(request.getDisplayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        if (user.getRole() == null) {
            user.setRole(UserRole.USER);
        }
        user.setActive(true);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    private String resolveRegistrationUsername(String requestedUsername, String email) {
        if (StringUtils.hasText(requestedUsername)) {
            return requestedUsername.trim();
        }

        String emailLocalPart = email.substring(0, email.indexOf('@'));
        String baseUsername = USERNAME_SAFE_CHARS.matcher(emailLocalPart.toLowerCase(Locale.ROOT))
                .replaceAll("_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        if (baseUsername.length() < 3) {
            baseUsername = "user_" + baseUsername;
        }
        if (baseUsername.length() > GENERATED_USERNAME_MAX_LENGTH) {
            baseUsername = baseUsername.substring(0, GENERATED_USERNAME_MAX_LENGTH);
        }

        String username = baseUsername;
        int suffix = 1;
        while (userRepository.existsByUsernameIgnoreCase(username)) {
            String suffixText = "_" + suffix++;
            int baseLength = Math.min(baseUsername.length(), GENERATED_USERNAME_MAX_LENGTH - suffixText.length());
            username = baseUsername.substring(0, baseLength) + suffixText;
        }

        return username;
    }

    public UserResponse getMyInfo() {
        return userMapper.toUserResponse(getCurrentUser());
    }

    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.WRONG_OLD_PASSWORD);
        }
        if (StringUtils.hasText(request.getConfirmPassword())
                && !request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.SAME_PASSWORD);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public UserProfileResponse searchUserByEmail(String email) {
        String currentUserId = getCurrentUserId();
        String normalizedEmail = normalizeEmail(email);

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (currentUserId.equals(user.getId()) || !user.isActive()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        return toUserProfileResponse(user, currentUserId);
    }

    public List<UserProfileResponse> searchUsersForFriendRequest(String keyword) {
        String currentUserId = getCurrentUserId();
        String normalizedKeyword = normalizeSearchKeyword(keyword);

        return userRepository.searchUsersForFriendRequest(
                        currentUserId,
                        normalizedKeyword,
                        PageRequest.of(0, 20))
                .stream()
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
        User user = getCurrentUser();

        user.setAvatarUrl(r2StorageService.uploadAvatar(user.getId(), file));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public String updateMyProfileAvatar(MultipartFile file) {
        return updateMyAvatar(file).getAvatarUrl();
    }

    public String updateMyProfileBackground(MultipartFile file) {
        User user = getCurrentUser();
        user.setBackgroundUrl(r2StorageService.uploadBackground(user.getId(), file));
        return userMapper.toUserResponse(userRepository.save(user)).getBackgroundUrl();
    }

    public UserResponse updateMyProfile(UserProfileUpdateRequest request) {
        User user = getCurrentUser();

        if (request.getName() != null) {
            user.setDisplayName(request.getName());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getDob() != null) {
            user.setDob(request.getDob());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getBackgroundUrl() != null) {
            user.setBackgroundUrl(request.getBackgroundUrl());
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse updateMySettings(UserSettingsUpdateRequest request) {
        User user = getCurrentUser();

        if (request.getShowBirthday() != null) {
            user.setShowBirthday(request.getShowBirthday());
        }
        if (request.getOnlineStatus() != null) {
            user.setOnlineStatus(request.getOnlineStatus());
        }
        if (request.getShowEmail() != null) {
            user.setShowEmail(request.getShowEmail());
        }
        if (request.getMentionSuggestions() != null) {
            user.setMentionSuggestions(request.getMentionSuggestions());
        }
        if (request.getReadReceipts() != null) {
            user.setReadReceipts(request.getReadReceipts());
        }
        if (request.getNotificationEnabled() != null) {
            user.setNotificationEnabled(request.getNotificationEnabled());
        }
        if (request.getSoundEnabled() != null) {
            user.setSoundEnabled(request.getSoundEnabled());
        }
        if (request.getNotificationSound() != null) {
            user.setNotificationSound(request.getNotificationSound().trim().toLowerCase(Locale.ROOT));
        }
        if (request.getThemeMode() != null) {
            user.setThemeMode(request.getThemeMode().trim().toLowerCase(Locale.ROOT));
        }
        if (request.getChatColor() != null) {
            user.setChatColor(request.getChatColor().trim());
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteMyAccount() {
        userRepository.delete(getCurrentUser());
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

    private User getCurrentUser() {
        return userRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new AppException(ErrorCode.SEARCH_QUERY_REQUIRED);
        }
        return email.trim().toLowerCase();
    }

    private String normalizeSearchKeyword(String keyword) {
        if (!StringUtils.hasText(keyword) || keyword.trim().length() < 2) {
            throw new AppException(ErrorCode.SEARCH_QUERY_REQUIRED);
        }
        return keyword.trim();
    }

    private UserProfileResponse toUserProfileResponse(User user, String currentUserId) {
        RelationshipService.RelationshipState relationship = relationshipService.resolve(currentUserId, user.getId());

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .backgroundUrl(user.getBackgroundUrl())
                .gender(user.getGender())
                .dob(user.getDob())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .bio(user.getBio())
                .friendshipStatus(relationship.status())
                .friendshipDirection(relationship.direction())
                .online(presenceService.isOnline(user.getId()))
                .build();
    }
}
