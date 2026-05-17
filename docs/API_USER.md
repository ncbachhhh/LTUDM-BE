# Tài Liệu API - User Module

Base URL:

```text
http://localhost:8080/api/v1
```

## 1. API xác thực

### 1.1 Đăng ký

```http
POST /auth/register
```

Request:

```json
{
  "email": "user@example.com",
  "username": "johndoe",
  "password": "password123",
  "display_name": "John Doe",
  "avatar_url": "https://example.com/avatar.jpg"
}
```

Response:

```json
{
  "code": 200,
  "data": {
    "id": "uuid-here",
    "email": "user@example.com",
    "username": "johndoe",
    "display_name": "John Doe",
    "avatar_url": "https://example.com/avatar.jpg",
    "created_at": "2026-03-02T10:30:00",
    "role": "USER",
    "is_active": true
  }
}
```

### 1.2 Đăng nhập

```http
POST /auth/login
```

Request:

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

Response:

```json
{
  "code": 200,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "isAuthenticated": true
  }
}
```

### 1.3 Làm mới token

```http
POST /auth/refresh
```

### 1.4 Đăng xuất

```http
POST /auth/logout
```

## 2. API người dùng

Header bắt buộc:

```http
Authorization: Bearer {accessToken}
```

### 2.1 Lấy thông tin cá nhân

```http
GET /users/me
```

### 2.2 Cập nhật thông tin user

```http
PATCH /users/{userId}
```

Chỉ chính chủ hoặc admin mới cập nhật được.

Request:

```json
{
  "display_name": "New Name",
  "avatar_url": "https://example.com/new-avatar.jpg"
}
```

### 2.3 Đổi mật khẩu

```http
POST /users/me/change-password
```

Request:

```json
{
  "old_password": "oldPassword123",
  "new_password": "newPassword456",
  "confirm_password": "newPassword456"
}
```

Response:

```json
{
  "code": 200,
  "data": "Đổi mật khẩu thành công."
}
```

### 2.4 Upload avatar

```http
PATCH /users/me/avatar
```

Header bắt buộc:

```http
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data
```

Form data:

| Field | Type | Bắt buộc | Mô tả |
|------|------|----------|------|
| `file` | File | Có | Ảnh avatar cần upload |

Quy tắc hiện tại:

- Chỉ hỗ trợ `image/jpeg`, `image/png`, `image/gif`, `image/webp`.
- Kích thước tối đa `5MB`.
- User phải đăng nhập.

Ví dụ `curl`:

```bash
curl --request PATCH "http://localhost:8080/api/v1/users/me/avatar" \
  --header "Authorization: Bearer <accessToken>" \
  --form "file=@D:/avatar.png"
```

Response:

```json
{
  "code": 200,
  "data": {
    "id": "uuid-here",
    "email": "user@example.com",
    "username": "johndoe",
    "display_name": "John Doe",
    "avatar_url": "https://<public-domain>/avatars/user-id/random-file.png",
    "created_at": "2026-03-02T10:30:00",
    "role": "USER",
    "is_active": true
  }
}
```

Lỗi có thể gặp:

| HTTP | Code | Message |
|------|------|---------|
| 400 | 400 | Vui lòng chọn file avatar |
| 400 | 400 | Avatar phải là ảnh JPG, PNG, GIF hoặc WEBP |
| 400 | 400 | Dung lượng avatar không được vượt quá 5MB |
| 401 | 401 | Chưa đăng nhập |
| 500 | 500 | Upload avatar thất bại |

Ghi chú:

- File được upload lên Cloudflare R2.
- Sau khi upload thành công, `avatar_url` trong bảng `users` sẽ được cập nhật.
- URL trả về nên dùng `R2_PUBLIC_BASE_URL` để frontend truy cập trực tiếp.

## 3. API admin

Các API admin hiện đã bị disable trong source code.

Các endpoint dưới đây chỉ còn mang tính tham chiếu lịch sử và hiện không active:

- `POST /admin/users`
- `PUT /admin/users/{userId}/ban`
- `PUT /admin/users/{userId}/unban`

Nếu bật lại `AdminController`, cần cập nhật tài liệu này đồng bộ với implementation thực tế.

## Mã lỗi

| Code | Message |
|------|---------|
| 400 | Email đã tồn tại / Username đã tồn tại |
| 400 | Dữ liệu không hợp lệ |
| 400 | Vui lòng chọn file avatar / Loại file không hợp lệ / File quá lớn |
| 401 | Chưa đăng nhập / Email hoặc mật khẩu không đúng |
| 403 | Không có quyền truy cập / Tài khoản đã bị khóa |
| 404 | Không tìm thấy người dùng |
| 500 | Lỗi máy chủ / Upload avatar thất bại |

## Ghi chú

- Response user hiện vẫn trả JSON theo snake_case: `display_name`, `avatar_url`, `created_at`, `is_active`.
- `username` tối đa 50 ký tự.
- `display_name` tối đa 100 ký tự.
- Upload avatar hiện dùng `multipart/form-data`, không gửi JSON body.
