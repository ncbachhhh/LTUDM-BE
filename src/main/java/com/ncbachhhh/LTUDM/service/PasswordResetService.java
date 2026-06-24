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

    // Khởi tạo reset password: tạo OTP, hash vào Redis, set cooldown và gửi mail nếu email tồn tại.
    public void forgotPassword(String email) {
        String normalizedEmail = normalizeEmail(email);
        userRepository.findByEmail(normalizedEmail).ifPresent(user -> {
            String cooldownKey = cooldownKey(normalizedEmail);
            if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
                return;
            }

            // Redis chỉ lưu OTP đã hash và có TTL ngắn để giảm rủi ro lộ OTP.
            String otp = generateOtp();
            redisTemplate.opsForValue().set(otpKey(normalizedEmail), passwordEncoder.encode(otp), OTP_TTL);
            redisTemplate.delete(attemptsKey(normalizedEmail));
            redisTemplate.opsForValue().set(cooldownKey, "1", COOLDOWN_TTL);

            emailService.sendPasswordResetOtp(user.getEmail(), otp);
        });
    }

    // Verify OTP reset password và cấp reset token tạm thời nếu OTP đúng.
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

        // Đếm số lần sai trong cùng TTL với OTP; qua giới hạn thì xóa OTP hiện tại.
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

        // Reset token được trả về client một lần, backend chỉ lưu hash của token trong Redis.
        String resetToken = generateResetToken();
        redisTemplate.opsForValue().set(resetTokenKey(resetToken), normalizedEmail, RESET_TOKEN_TTL);
        redisTemplate.delete(otpKey);
        redisTemplate.delete(attemptsKey);

        return ResetOtpVerificationResponse.builder()
                .resetToken(resetToken)
                .build();
    }

    // Đổi mật khẩu bằng reset token và xóa toàn bộ Redis key của luong reset password.
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

    // Tạo OTP 6 chu số bằng SecureRandom.
    private String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    // Tạo reset token random URL-safe, dùng làm proof sau khi verify OTP.
    private String generateResetToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // Normalize email để Redis key và query database không bị lệch hoa/thường.
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    // Parse attempts từ Redis; dữ liệu hỏng thì fallback 0 để không làm crash flow.
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

    // Khóa OTP hiện tại bằng cách xóa OTP và attempts, buoc user xin OTP mới.
    private void lockCurrentOtp(String email) {
        redisTemplate.delete(otpKey(email));
        redisTemplate.delete(attemptsKey(email));
    }

    // Key lưu hash OTP reset password theo email.
    private String otpKey(String email) {
        return "forgot:otp:" + email;
    }

    // Key cooldown ngắn để hạn chế spam email OTP.
    private String cooldownKey(String email) {
        return "forgot:cooldown:" + email;
    }

    // Key đếm số lần verify OTP sai.
    private String attemptsKey(String email) {
        return "forgot:attempts:" + email;
    }

    // Key lưu reset token đã hash, tránh lưu raw reset token trong Redis.
    private String resetTokenKey(String resetToken) {
        return "forgot:reset-token:" + sha256(resetToken);
    }

    // Hash reset token để token raw chỉ nằm ở client trong thời gian ngắn.
    @SneakyThrows
    private String sha256(String value) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }
}
