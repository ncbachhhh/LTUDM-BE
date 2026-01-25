package com.ncbachhhh.LTUDM.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Getter
@Setter
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;              // ID dùng UUID tự động sinh
    private String email;           // email người dùng
    private String username;        // tên đăng nhập
    private String password_hash;   // mật khẩu đã mã hóa
    private String display_name;    // tên hiển thị
    private String avatar_url;      // URL ảnh đại diện
    private LocalDate created_at;   // ngày tạo tài khoản
}
