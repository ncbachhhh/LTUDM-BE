package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.config.R2Properties;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class R2StorageService {
    public static final long MAX_AVATAR_SIZE_BYTES = 5L * 1024 * 1024;
    public static final long MAX_MESSAGE_IMAGE_SIZE_BYTES = 10L * 1024 * 1024;
    public static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final Map<String, String> CONTENT_TYPE_EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/gif", "gif",
            "image/webp", "webp"
    );

    S3Client s3Client;
    R2Properties r2Properties;

    public String uploadAvatar(String userId, MultipartFile file) {
        return uploadFile(
                "avatars/%s".formatted(userId),
                file,
                IMAGE_CONTENT_TYPES,
                MAX_AVATAR_SIZE_BYTES,
                ErrorCode.AVATAR_FILE_REQUIRED,
                ErrorCode.INVALID_AVATAR_FILE_TYPE,
                ErrorCode.AVATAR_FILE_TOO_LARGE,
                ErrorCode.AVATAR_UPLOAD_FAILED
        );
    }

    public String uploadMessageImage(String conversationId, String senderId, MultipartFile file) {
        return uploadFile(
                "messages/%s/%s".formatted(conversationId, senderId),
                file,
                IMAGE_CONTENT_TYPES,
                MAX_MESSAGE_IMAGE_SIZE_BYTES,
                ErrorCode.MESSAGE_IMAGE_FILE_REQUIRED,
                ErrorCode.INVALID_MESSAGE_IMAGE_FILE_TYPE,
                ErrorCode.MESSAGE_IMAGE_FILE_TOO_LARGE,
                ErrorCode.MESSAGE_IMAGE_UPLOAD_FAILED
        );
    }

    public String uploadFile(
            String folder,
            MultipartFile file,
            Set<String> allowedContentTypes,
            long maxFileSizeBytes,
            ErrorCode fileRequiredError,
            ErrorCode invalidFileTypeError,
            ErrorCode fileTooLargeError,
            ErrorCode uploadFailedError
    ) {
        validateFile(file, allowedContentTypes, maxFileSizeBytes, fileRequiredError, invalidFileTypeError, fileTooLargeError);

        String contentType = file.getContentType();
        String extension = resolveExtension(contentType, invalidFileTypeError);
        String normalizedFolder = normalizeFolder(folder);
        String objectKey = normalizedFolder + "/" + UUID.randomUUID() + "." + extension;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(r2Properties.bucket())
                    .key(objectKey)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return buildFileUrl(objectKey);
        } catch (IOException | S3Exception ex) {
            throw new AppException(uploadFailedError);
        }
    }

    private void validateFile(
            MultipartFile file,
            Set<String> allowedContentTypes,
            long maxFileSizeBytes,
            ErrorCode fileRequiredError,
            ErrorCode invalidFileTypeError,
            ErrorCode fileTooLargeError
    ) {
        if (file == null || file.isEmpty()) {
            throw new AppException(fileRequiredError);
        }

        String contentType = file.getContentType();
        if (!allowedContentTypes.contains(contentType)) {
            throw new AppException(invalidFileTypeError);
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new AppException(fileTooLargeError);
        }
    }

    private String resolveExtension(String contentType, ErrorCode invalidFileTypeError) {
        String extension = CONTENT_TYPE_EXTENSIONS.get(contentType);
        if (extension == null) {
            throw new AppException(invalidFileTypeError);
        }
        return extension;
    }

    private String normalizeFolder(String folder) {
        if (!StringUtils.hasText(folder)) {
            throw new IllegalArgumentException("Upload folder must not be blank");
        }

        return folder.replace('\\', '/').replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String buildFileUrl(String objectKey) {
        if (StringUtils.hasText(r2Properties.publicBaseUrl())) {
            return r2Properties.publicBaseUrl().replaceAll("/+$", "") + "/" + objectKey;
        }

        return r2Properties.endpoint().replaceAll("/+$", "")
                + "/" + r2Properties.bucket()
                + "/" + objectKey;
    }
}
