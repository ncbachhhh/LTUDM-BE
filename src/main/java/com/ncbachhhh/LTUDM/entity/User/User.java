package com.ncbachhhh.LTUDM.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "background_url", length = 500)
    private String backgroundUrl;

    @Column(length = 20)
    private String gender;

    private LocalDate dob;

    @Column(length = 20)
    private String phone;

    @Column(length = 50)
    private String nickname;

    @Column(length = 500)
    private String bio;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "show_birthday", nullable = false, length = 10)
    private String showBirthday = "full";

    @Column(name = "online_status", nullable = false)
    private boolean onlineStatus = true;

    @Column(name = "show_email", nullable = false)
    private boolean showEmail = true;

    @Column(name = "mention_suggestions", nullable = false)
    private boolean mentionSuggestions = true;

    @Column(name = "read_receipts", nullable = false)
    private boolean readReceipts = true;

    @Column(name = "notification_enabled", nullable = false)
    private boolean notificationEnabled = true;

    @Column(name = "sound_enabled", nullable = false)
    private boolean soundEnabled = true;

    @Column(name = "notification_sound", nullable = false, length = 100)
    private String notificationSound = "default";

    @Column(name = "theme_mode", nullable = false, length = 10)
    private String themeMode = "light";

    @Column(name = "chat_color", nullable = false, length = 500)
    private String chatColor = "#0A84FF";
}
