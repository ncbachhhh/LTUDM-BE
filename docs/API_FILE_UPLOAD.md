# Tài Liệu API - File Upload

Base URL:

```text
http://localhost:8080/api/v1
```

## 1. Tổng quan

Backend hiện dùng Cloudflare R2 để lưu file upload.

Thiết kế hiện tại chia thành 2 lớp:

- `R2StorageService.uploadFile(...)`: hàm upload file dùng chung cho nhiều nghiệp vụ.
- `R2StorageService.uploadAvatar(...)`: wrapper cho avatar, gọi lại hàm upload chung với rule riêng.

Mục tiêu của cách tách này:

- Không lặp lại logic validate file.
- Không lặp lại logic generate object key.
- Dễ mở rộng sang ảnh chat, file đính kèm hoặc tài liệu.

## 2. Cấu hình môi trường

Các biến môi trường cần có:

```env
R2_ENDPOINT=https://<account-id>.r2.cloudflarestorage.com
R2_BUCKET=<bucket-name>
R2_ACCESS_KEY=<access-key>
R2_SECRET_KEY=<secret-key>
R2_PUBLIC_BASE_URL=https://<public-domain>
```

Ghi chú:

- `R2_ENDPOINT` là S3 endpoint của Cloudflare R2.
- `R2_BUCKET` là bucket lưu file.
- `R2_PUBLIC_BASE_URL` nên là domain public để frontend truy cập trực tiếp ảnh/file.
- Nếu không set `R2_PUBLIC_BASE_URL`, backend sẽ fallback sang URL dạng `endpoint/bucket/object-key`, thường không phù hợp để public trực tiếp.

## 3. Cấu hình multipart

Trong `application.yaml`:

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 5MB
```

Giới hạn global hiện tại là `5MB`.

## 4. Cách upload file trong code

### 4.1 Hàm upload dùng chung

`R2StorageService` đã có hàm:

```java
public String uploadFile(
    String folder,
    MultipartFile file,
    Set<String> allowedContentTypes,
    long maxFileSizeBytes,
    ErrorCode fileRequiredError,
    ErrorCode invalidFileTypeError,
    ErrorCode fileTooLargeError,
    ErrorCode uploadFailedError
)
```

Ý nghĩa tham số:

- `folder`: thư mục logical trong bucket, ví dụ `avatars/{userId}` hoặc `messages/{conversationId}`.
- `file`: file nhận từ multipart request.
- `allowedContentTypes`: danh sách MIME type được phép.
- `maxFileSizeBytes`: dung lượng tối đa.
- Các `ErrorCode`: rule lỗi theo từng nghiệp vụ.

Hàm này sẽ:

1. Validate file rỗng.
2. Validate MIME type.
3. Validate dung lượng.
4. Tạo object key ngẫu nhiên.
5. Upload file lên Cloudflare R2.
6. Trả về public URL của file.

### 4.2 Ví dụ dùng cho ảnh chat sau này

```java
String imageUrl = r2StorageService.uploadFile(
    "messages/" + conversationId,
    file,
    R2StorageService.IMAGE_CONTENT_TYPES,
    10L * 1024 * 1024,
    ErrorCode.FILE_REQUIRED,
    ErrorCode.INVALID_FILE_TYPE,
    ErrorCode.FILE_TOO_LARGE,
    ErrorCode.FILE_UPLOAD_FAILED
);
```

Nếu sau này hỗ trợ nhiều loại file hơn, có thể tạo thêm các tập MIME type riêng:

- `IMAGE_CONTENT_TYPES`
- `DOCUMENT_CONTENT_TYPES`
- `MEDIA_CONTENT_TYPES`

## 5. Quy ước object key

Object key hiện được tạo theo dạng:

```text
{folder}/{uuid}.{extension}
```

Ví dụ:

```text
avatars/9b5d8d30-7d23-4f5f-b4aa-497af89d9131/3caa6e1f-4f77-44af-95d8-c2f5a7db0db0.png
```

Lợi ích:

- Tránh trùng tên file.
- Không phụ thuộc tên file người dùng upload.
- Dễ phân tách file theo nghiệp vụ.

## 6. MIME type ảnh đang hỗ trợ

Hiện tại `R2StorageService` cho phép các loại ảnh:

- `image/jpeg`
- `image/png`
- `image/gif`
- `image/webp`

## 7. Lưu ý khi mở rộng

- Không nên đưa rule nghiệp vụ vào controller.
- Nên để controller chỉ nhận `MultipartFile`, còn service quyết định rule validate.
- Nên tạo wrapper riêng theo use case như `uploadAvatar`, `uploadMessageImage`, `uploadAttachment`.
- Nếu cần xóa file cũ, nên bổ sung thêm hàm `deleteFile(objectKey)` trong `R2StorageService`.

## 8. Hướng dẫn upload avatar

Avatar hiện là use case đầu tiên đang dùng cơ chế upload file chung.

Chi tiết endpoint và cách gọi xem tại:

- [API_USER.md](./API_USER.md)
