package com.ncbachhhh.LTUDM.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttachmentResponse {
    private String id;

    @JsonProperty("file_url")
    private String fileUrl;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("mime_type")
    private String mimeType;

    @JsonProperty("file_size")
    private Long fileSize;
}
