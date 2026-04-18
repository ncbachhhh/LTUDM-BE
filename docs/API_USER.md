# API Documentation - User Module

Base URL: `http://localhost:8080/api/v1`

## 1. Authentication APIs

### 1.1 Dang ky
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

### 1.2 Dang nhap
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

### 1.5 Kiem tra token
```http
POST /auth/introspect
```

## 2. User APIs

Header bat buoc:
```http
Authorization: Bearer {accessToken}
```

### 2.1 Lay thong tin ca nhan
```http
GET /users/me
```

### 2.2 Lay thong tin user theo ID
```http
GET /users/{userId}
```

Chi chinh chu hoac admin moi truy cap duoc.

### 2.3 Cap nhat thong tin user
```http
PATCH /users/{userId}
```

Chi chinh chu hoac admin moi cap nhat duoc.

Request:
```json
{
  "display_name": "New Name",
  "avatar_url": "https://example.com/new-avatar.jpg"
}
```

### 2.4 Doi mat khau
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

### 2.5 Upload avatar
```http
PATCH /users/me/avatar
```

Header bat buoc:
```http
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data
```

Form data:

| Field | Type | Required | Description |
|------|------|----------|-------------|
| `file` | File | Yes | Anh avatar can upload |

Rule hien tai:
- Chi ho tro `image/jpeg`, `image/png`, `image/gif`, `image/webp`
- Kich thuoc toi da `5MB`
- User phai dang nhap

Vi du `curl`:

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

Loi co the gap:

| HTTP | Code | Message |
|------|------|---------|
| 400 | 400 | Avatar file is required |
| 400 | 400 | Avatar must be a JPG, PNG, GIF, or WEBP image |
| 400 | 400 | Avatar file size must not exceed 5MB |
| 401 | 401 | Unauthenticated |
| 500 | 500 | Failed to upload avatar |

Ghi chu:
- File duoc upload len Cloudflare R2
- Sau khi upload thanh cong, `avatar_url` trong bang `users` se duoc cap nhat
- URL tra ve nen dung `R2_PUBLIC_BASE_URL` de frontend truy cap truc tiep

## 3. Admin APIs

Các API admin hien da bi disable trong source code.

Các endpoint duoi day chi con mang tinh tham chieu lich su va hien khong active:
- `POST /admin/users`
- `PUT /admin/users/{userId}/ban`
- `PUT /admin/users/{userId}/unban`

Neu bat lai `AdminController`, can cap nhat tai lieu nay dong bo voi implementation thuc te.

## Error codes

| Code | Message |
|------|---------|
| 400 | Email/Username already exists |
| 400 | Validation failed |
| 400 | Avatar file is required / invalid type / too large |
| 401 | Unauthenticated / Wrong password |
| 403 | Access denied / User banned |
| 404 | User not found |
| 500 | Internal server error / Failed to upload avatar |

## Ghi chu

- Response user hien van tra JSON theo snake_case: `display_name`, `avatar_url`, `created_at`, `is_active`.
- `username` toi da 50 ky tu.
- `display_name` toi da 100 ky tu.
- Upload avatar hien dung `multipart/form-data`, khong gui JSON body.
