package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.request.AuthenticationRequest;
import com.ncbachhhh.LTUDM.entity.User.User;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean authenticate(AuthenticationRequest request) {
        var userOpt = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return passwordEncoder.matches(request.getPassword(), userOpt.getPassword_hash());
    }
}
