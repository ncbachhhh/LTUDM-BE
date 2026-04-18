# API Documentation - File Upload

Base URL: `http://localhost:8080/api/v1`

## 1. Tong quan

Backend hien dung Cloudflare R2 de luu file upload.

Thiet ke hien tai chia lam 2 lop:
- `R2StorageService.uploadFile(...)`: ham upload file dung chung cho nhieu nghiep vu
- `R2StorageService.uploadAvatar(...)`: wrapper cho avatar, goi lai ham upload chung voi rule rieng

Muc tieu cua cach tach nay la:
- khong lap lai logic validate file
- khong lap lai logic generate object key
- de mo rong sang anh chat, file dinh kem, tai lieu

## 2. Cau hinh moi truong

Các biến môi trường cần có:

```env
R2_ENDPOINT=https://<account-id>.r2.cloudflarestorage.com
R2_BUCKET=<bucket-name>
R2_ACCESS_KEY=<access-key>
R2_SECRET_KEY=<secret-key>
R2_PUBLIC_BASE_URL=https://<public-domain>
```

Ghi chu:
- `R2_ENDPOINT` la S3 endpoint cua Cloudflare R2
- `R2_BUCKET` la bucket luu file
- `R2_PUBLIC_BASE_URL` nen la domain public de frontend co the truy cap truc tiep anh/file
- neu khong set `R2_PUBLIC_BASE_URL`, backend se fallback sang URL dang `endpoint/bucket/object-key`, URL nay thuong khong phu hop de public truc tiep

## 3. Cau hinh multipart

Trong `application.yaml`:

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 5MB
```

Hien tai gioi han global dang la `5MB`.

## 4. Cách upload file trong code

### 4.1 Ham upload dung chung

`R2StorageService` da co ham:

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

Y nghia tham so:
- `folder`: thu muc logical trong bucket, vi du `avatars/{userId}` hoac `messages/{conversationId}`
- `file`: file nhan tu multipart request
- `allowedContentTypes`: danh sach MIME type duoc phep
- `maxFileSizeBytes`: dung luong toi da
- cac `ErrorCode`: rule loi theo tung nghiep vu

Ham nay se:
1. validate file rong
2. validate MIME type
3. validate dung luong
4. tao object key ngau nhien
5. upload file len Cloudflare R2
6. tra ve public URL cua file

### 4.2 Vi du dung cho anh chat sau nay

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

Neu sau nay ho tro nhieu loai file hon, co the tao them cac tap MIME type rieng nhu:
- `IMAGE_CONTENT_TYPES`
- `DOCUMENT_CONTENT_TYPES`
- `MEDIA_CONTENT_TYPES`

## 5. Quy uoc object key

Object key hien duoc tao theo dang:

```text
{folder}/{uuid}.{extension}
```

Vi du:

```text
avatars/9b5d8d30-7d23-4f5f-b4aa-497af89d9131/3caa6e1f-4f77-44af-95d8-c2f5a7db0db0.png
```

Loi ich:
- tranh trung ten file
- khong phu thuoc ten file nguoi dung upload
- de phan tach file theo nghiep vu

## 6. MIME type anh dang ho tro

Hien tai `R2StorageService` cho phep cac loai anh:
- `image/jpeg`
- `image/png`
- `image/gif`
- `image/webp`

## 7. Luu y khi mo rong

- Khong nen dua rule nghiep vu vao controller
- Nen de controller chi nhan `MultipartFile`, con service quyet dinh rule validate
- Nen tao wrapper rieng theo use case nhu `uploadAvatar`, `uploadMessageImage`, `uploadAttachment`
- Neu can xoa file cu, nen bo sung them ham `deleteFile(objectKey)` trong `R2StorageService`

## 8. Huong dan upload avatar

Avatar hien la use case dau tien dang dung co che upload file chung.

Chi tiet endpoint va cach goi xem o:
- [API_USER.md](./API_USER.md)
