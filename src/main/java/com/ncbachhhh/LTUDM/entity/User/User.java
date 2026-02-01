package com.ncbachhhh.LTUDM.entity.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Getter
@Setter

@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;              // ID dùng UUID tự động sinh
    private String email;           // email người dùng
    private String username;        // tên đăng nhập
    private String password_hash;   // mật khẩu đã mã hóa
    private String display_name;    // tên hiển thị
    private String avatar_url;      // URL ảnh đại diện
    private LocalDate created_at = LocalDate.now();   // ngày tạo tài khoản
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;          // vai trò người dùng: USER, ADMIN
}
