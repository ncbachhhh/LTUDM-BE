package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.request.UserLoginRequest;
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

    // Đăng nhập người dùng
//    public User loginUser(UserLoginRequest request) {
//        User user = userRepository.findByEmail(request.getEmail());
//        if (user == null) {
//            throw new AppException(ErrorCode.EMAIL_NOT_FOUND);
//        }
//
//        if (!passwordEncoder.matches(request.getPassword(), user.getPassword_hash())) {
//            throw new AppException(ErrorCode.WRONG_PASSWORD);
//        }
//
//        return user;
//    }

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

        String password_hash = passwordEncoder.encode(request.getPassword());
        user.setPassword_hash(password_hash);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    // Xóa người dùng
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.set_active(false);
        userRepository.save(user);
    }
}
