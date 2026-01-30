package com.ncbachhhh.LTUDM.entity.Friendship;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Setter
@Getter

@Table(name = "friendships")
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String requester_id;  // ID người gửi lời mời kết bạn
    private String addressee_id;  // ID người nhận lời mời kết bạn
    @Enumerated(EnumType.STRING)
    private FriendshipStatus status; // Trạng thái kết bạn: PENDING, ACCEPTED, DECLINED, BLOCKED
    private LocalDate created_at;    // Ngày tạo lời mời kết bạn
    private LocalDate updated_at;    // Ngày cập nhật trạng thái kết bạn
}
