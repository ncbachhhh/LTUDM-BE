package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.request.UserLoginRequest;
import com.ncbachhhh.LTUDM.dto.request.UserRegisterRequest;
import com.ncbachhhh.LTUDM.entity.User.UserRole;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.repository.UserRepository;
import com.ncbachhhh.LTUDM.entity.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(UserRegisterRequest request) {
        User user = new User();

        if (userRepository.findByEmail(request.getEmail()) != null)
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);

        if (userRepository.findByUsername(request.getUsername()) != null)
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);

        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setDisplay_name(request.getDisplay_name());
        user.setAvatar_url(request.getAvatar_url());
        user.setCreated_at(java.time.LocalDate.now());
        user.setRole(UserRole.USER);

        // mã hóa mật khẩu trước khi lưu
        String password_hash = passwordEncoder.encode(request.getPassword());
        user.setPassword_hash(password_hash);

        return userRepository.save(user);
    }

    public User loginUser(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword_hash())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }

        return user;
    }
}
