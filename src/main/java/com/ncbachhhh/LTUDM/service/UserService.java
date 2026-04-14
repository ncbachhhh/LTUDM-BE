package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.request.ChangePasswordRequest;
import com.ncbachhhh.LTUDM.dto.request.UserRegisterRequest;
import com.ncbachhhh.LTUDM.dto.request.UserUpdateRequest;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;

    // Tạo tài khoản người dùng mới
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

    // Lấy thông tin của user đang đăng nhập
    public UserResponse getMyInfo() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return userMapper.toUserResponse(userRepository.findById(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    // Đổi mật khẩu cho user hiện tại sau khi kiểm tra mật khẩu cũ
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

    // Lấy thông tin user theo id
    public UserResponse getUserById(String userId) {
        return userMapper.toUserResponse(userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    // Cập nhật thông tin cơ bản của user
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateUser(request, user);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    // Khóa tài khoản người dùng
    public void banUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setActive(false);
        userRepository.save(user);
    }

    // Mở khóa tài khoản người dùng
    public void unbanUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setActive(true);
        userRepository.save(user);
    }
}
