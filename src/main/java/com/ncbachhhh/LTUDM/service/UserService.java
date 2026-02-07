package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.request.ChangePasswordRequest;
import com.ncbachhhh.LTUDM.dto.request.UserRegisterRequest;
import com.ncbachhhh.LTUDM.dto.request.UserUpdateRequest;
import com.ncbachhhh.LTUDM.dto.response.UserResponse;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.mapper.UserMapper;
import com.ncbachhhh.LTUDM.repository.UserRepository;
import com.ncbachhhh.LTUDM.entity.User.User;
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

    // Tạo người dùng mới
    public UserResponse createUser(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);

        if (userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);

        User user = userMapper.toUser(request);

        // mã hóa mật khẩu trước khi lưu
        String password_hash = passwordEncoder.encode(request.getPassword());
        user.setPassword_hash(password_hash);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    // Lấy thông tin người dùng hiện tại từ token
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();

        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String userId = authentication.getName();

        return userMapper.toUserResponse(userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    public void changePassword(ChangePasswordRequest request) {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();

        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String userId = authentication.getName();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOld_password(), user.getPassword_hash())) {
            throw new AppException(ErrorCode.WRONG_OLD_PASSWORD);
        }
        
        String new_password_hash  = passwordEncoder.encode(request.getNew_password());
        user.setPassword_hash(new_password_hash);
        userRepository.save(user);
    }

    // Lấy thông tin người dùng theo ID
    public UserResponse getUserById(String userId) {
        return userMapper.toUserResponse(userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    // Cập nhật thông tin người dùng
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateUser(request, user);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    // Xóa người dùng
    public void banUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.set_active(false);
        userRepository.save(user);
    }

    // Unbanned user
    public void unbanUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.set_active(true);
        userRepository.save(user);
    }

}
