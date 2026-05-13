package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.response.ResetOtpVerificationResponse;
import com.ncbachhhh.LTUDM.entity.User.User;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PasswordResetService {
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(10);
    private static final Duration COOLDOWN_TTL = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 5;

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    StringRedisTemplate redisTemplate;
    EmailService emailService;
    SecureRandom secureRandom = new SecureRandom();

    public void forgotPassword(String email) {
        String normalizedEmail = normalizeEmail(email);
        userRepository.findByEmail(normalizedEmail).ifPresent(user -> {
            String cooldownKey = cooldownKey(normalizedEmail);
            if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
                return;
            }

            String otp = generateOtp();
            redisTemplate.opsForValue().set(otpKey(normalizedEmail), passwordEncoder.encode(otp), OTP_TTL);
            redisTemplate.delete(attemptsKey(normalizedEmail));
            redisTemplate.opsForValue().set(cooldownKey, "1", COOLDOWN_TTL);

            emailService.sendPasswordResetOtp(user.getEmail(), otp);
        });
    }

    public ResetOtpVerificationResponse verifyResetOtp(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.findByEmail(normalizedEmail).isEmpty()) {
            throw new AppException(ErrorCode.INVALID_RESET_OTP);
        }

        String otpKey = otpKey(normalizedEmail);
        String otpHash = redisTemplate.opsForValue().get(otpKey);
        if (otpHash == null) {
            throw new AppException(ErrorCode.INVALID_RESET_OTP);
        }

        String attemptsKey = attemptsKey(normalizedEmail);
        int attempts = parseAttempts(redisTemplate.opsForValue().get(attemptsKey));
        if (attempts >= MAX_ATTEMPTS) {
            lockCurrentOtp(normalizedEmail);
            throw new AppException(ErrorCode.RESET_OTP_TOO_MANY_ATTEMPTS);
        }

        if (!passwordEncoder.matches(otp, otpHash)) {
            Long newAttempts = redisTemplate.opsForValue().increment(attemptsKey);
            redisTemplate.expire(attemptsKey, OTP_TTL);
            if (newAttempts != null && newAttempts >= MAX_ATTEMPTS) {
                lockCurrentOtp(normalizedEmail);
            }
            throw new AppException(ErrorCode.INVALID_RESET_OTP);
        }

        String resetToken = generateResetToken();
        redisTemplate.opsForValue().set(resetTokenKey(resetToken), normalizedEmail, RESET_TOKEN_TTL);
        redisTemplate.delete(otpKey);
        redisTemplate.delete(attemptsKey);

        return ResetOtpVerificationResponse.builder()
                .resetToken(resetToken)
                .build();
    }

    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        String tokenKey = resetTokenKey(resetToken);
        String email = redisTemplate.opsForValue().get(tokenKey);
        if (email == null) {
            throw new AppException(ErrorCode.INVALID_RESET_TOKEN);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redisTemplate.delete(tokenKey);
        redisTemplate.delete(otpKey(email));
        redisTemplate.delete(attemptsKey(email));
        redisTemplate.delete(cooldownKey(email));
    }

    private String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private String generateResetToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private int parseAttempts(String value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void lockCurrentOtp(String email) {
        redisTemplate.delete(otpKey(email));
        redisTemplate.delete(attemptsKey(email));
    }

    private String otpKey(String email) {
        return "forgot:otp:" + email;
    }

    private String cooldownKey(String email) {
        return "forgot:cooldown:" + email;
    }

    private String attemptsKey(String email) {
        return "forgot:attempts:" + email;
    }

    private String resetTokenKey(String resetToken) {
        return "forgot:reset-token:" + sha256(resetToken);
    }

    @SneakyThrows
    private String sha256(String value) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }
}
