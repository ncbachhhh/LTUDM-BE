package com.ncbachhhh.LTUDM.entity.Attachment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "attachments")
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "message_id", nullable = false, length = 36)
    private String messageId;

    @Column(name = "file_url", length = 700)
    private String fileUrl;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "mime_type", length = 120)
    private String mimeType;

    @Column(name = "file_size")
    private Long fileSize;
}
