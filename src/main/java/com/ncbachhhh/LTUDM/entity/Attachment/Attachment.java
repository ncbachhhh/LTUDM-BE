package com.ncbachhhh.LTUDM.entity.Attachment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter

@Table(name = "attachments")
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;              // ID dùng UUID tự động sinh
    private String message_id;      // ID tin nhắn liên kết
    private String file_name;       // Tên tệp tin
    private String file_url;        // URL tệp tin
    private String mime_type;       // Loại MIME của tệp tin
    private long file_size;         // Kích thước tệp tin (tính bằng byte)
}
