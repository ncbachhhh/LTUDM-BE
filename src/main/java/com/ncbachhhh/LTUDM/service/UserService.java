package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.request.UserCreationRequest;
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

    public User createUser(UserCreationRequest request) {
        User user = new User();

        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setDisplay_name(request.getDisplay_name());
        user.setAvatar_url(request.getAvatar_url());
        user.setCreated_at(java.time.LocalDate.now());

        // mã hóa mật khẩu trước khi lưu
        String password_hash = passwordEncoder.encode(request.getPassword());
        user.setPassword_hash(password_hash);

        return userRepository.save(user);
    }
}
