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

## 2.6 Tim kiem va xem profile user khac

### 2.6.1 Tim kiem user bang email

```http
GET /users/search-by-email?email={email}
```

Mo ta:

- Tim user khac bang email chinh xac.
- Chi tra user dang active va khac user hien tai.
- Khong tra ve user hien tai.
- `email` bat buoc.

Response mau:

```json
{
  "code": 200,
  "data": {
    "id": "uuid-user",
    "email": "friend@example.com",
    "username": "friend",
    "display_name": "Friend User",
    "avatar_url": null,
    "friendship_status": "NONE",
    "friendship_direction": "NONE"
  }
}
```

### 2.6.2 Xem profile user khac

```http
GET /users/{userId}/profile
```

Mo ta:

- Xem profile public cua user khac.
- Khong cho xem profile cua chinh minh qua endpoint nay; dung `GET /users/me`.
- User phai ton tai va dang active.
- Response co `friendship_status` va `friendship_direction` de frontend hien nut phu hop.

Response mau:

```json
{
  "code": 200,
  "data": {
    "id": "uuid-user",
    "email": "friend@example.com",
    "username": "friend",
    "display_name": "Friend User",
    "avatar_url": null,
    "friendship_status": "PENDING",
    "friendship_direction": "INCOMING"
  }
}
```

`friendship_status` co the la `NONE`, `PENDING`, `ACCEPTED`, `DECLINED`, `BLOCKED`.
`friendship_direction` co the la `NONE`, `INCOMING`, `OUTGOING`; chi co y nghia khi `friendship_status = PENDING`.

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
