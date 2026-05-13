package com.ncbachhhh.LTUDM.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {
    JavaMailSender mailSender;

    public void sendPasswordResetOtp(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password reset OTP");
        message.setText("""
                Your password reset OTP is: %s

                This OTP is valid for 5 minutes. If you did not request a password reset, ignore this email.
                """.formatted(otp));

        mailSender.send(message);
    }
}
