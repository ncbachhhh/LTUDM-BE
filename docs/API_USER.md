# API Documentation - User Module

Base URL: `http://localhost:8080/api/v1`

## 1. Authentication APIs

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

### 1.3 Refresh token
```http
POST /auth/refresh
```

### 1.4 Logout
```http
POST /auth/logout
```

### 1.5 Kiểm tra token
```http
POST /auth/introspect
```

## 2. User APIs

Header bắt buộc:
```http
Authorization: Bearer {accessToken}
```

### 2.1 Lấy thông tin cá nhân
```http
GET /users/me
```

### 2.2 Lấy thông tin user theo ID
```http
GET /users/{userId}
```

Chỉ chính chủ hoặc admin mới truy cập được.

### 2.3 Cập nhật thông tin user
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

### 2.4 Đổi mật khẩu
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
  "data": "Password changed successfully"
}
```

## 3. Admin APIs

Các API admin hiện đã bị disable trong source code.

Các endpoint dưới đây chỉ còn mang tính tham chiếu lịch sử và hiện không active:
- `POST /admin/users`
- `PUT /admin/users/{userId}/ban`
- `PUT /admin/users/{userId}/unban`

Nếu bật lại `AdminController`, cần cập nhật tài liệu này đồng bộ với implementation thực tế.

## Error codes

| Code | Message |
|------|---------|
| 400 | Email/Username already exists |
| 400 | Validation failed |
| 401 | Unauthenticated / Wrong password |
| 403 | Access denied / User banned |
| 404 | User not found |
| 500 | Internal server error |

## Ghi chú

- Response user hiện vẫn trả JSON theo snake_case: `display_name`, `avatar_url`, `created_at`, `is_active`.
- `username` tối đa 50 ký tự.
- `display_name` tối đa 100 ký tự.
