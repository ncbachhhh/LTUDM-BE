# API Documentation - User Module

**Base URL:** `http://localhost:8080`

---

## 1. Authentication APIs

### 1.1 Đăng ký

```
POST /auth/register
```

**Request:**
```json
{
    "email": "user@example.com",
    "username": "johndoe",
    "password": "password123",
    "display_name": "John Doe",
    "avatar_url": "https://example.com/avatar.jpg"
}
```

**Response:**
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

---

### 1.2 Đăng nhập

```
POST /auth/login
```

**Request:**
```json
{
    "email": "user@example.com",
    "password": "password123"
}
```

**Response:**
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

---

### 1.3 Refresh Token

```
POST /auth/refresh
```

**Request:**
```json
{
    "refreshToken": "eyJhbGci..."
}
```

**Response:** Giống login

---

### 1.4 Logout

```
POST /auth/logout
```

**Request:**
```json
{
    "token": "eyJhbGci..."
}
```

**Response:**
```json
{
    "code": 200,
    "message": "Logged out successfully"
}
```

---

### 1.5 Kiểm tra Token

```
POST /auth/introspect
```

**Request:**
```json
{
    "token": "eyJhbGci..."
}
```

**Response:**
```json
{
    "code": 200,
    "data": {
        "valid": true
    }
}
```

---

## 2. User APIs

> **Header:** `Authorization: Bearer {accessToken}`

### 2.1 Lấy thông tin cá nhân

```
GET /users/me
```

**Response:**
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

---

### 2.2 Lấy thông tin user theo ID

```
GET /users/{userId}
```

> Chỉ chính chủ hoặc admin mới truy cập được

**Response:** Giống `/users/me`

---

### 2.3 Cập nhật thông tin user

```
PATCH /users/{userId}
```

> Chỉ chính chủ hoặc admin mới cập nhật được

**Request:**
```json
{
    "display_name": "New Name",
    "avatar_url": "https://example.com/new-avatar.jpg"
}
```

**Response:** Trả về thông tin user sau khi cập nhật

---

### 2.4 Đổi mật khẩu

```
POST /users/me/change-password
```

**Request:**
```json
{
    "old_password": "oldPassword123",
    "new_password": "newPassword456",
    "confirm_password": "newPassword456"
}
```

**Response:**
```json
{
    "code": 200,
    "data": "Password changed successfully"
}
```

---

## 3. Admin APIs

> **Yêu cầu:** Role ADMIN  
> **Header:** `Authorization: Bearer {accessToken}`

### 3.1 Tạo user mới

```
POST /admin/users
```

**Request:** Giống đăng ký

**Response:** Trả về thông tin user đã tạo

---

### 3.2 Ban user

```
PATCH /admin/users/{userId}/ban
```

**Response:**
```json
{
    "code": 200,
    "data": "Successfully banned user with ID: uuid-here"
}
```

---

### 3.3 Unban user

```
PATCH /admin/users/{userId}/unban
```

**Response:**
```json
{
    "code": 200,
    "data": "Successfully unbanned user with ID: uuid-here"
}
```

---

## Error Codes

| Code | Message |
|------|---------|
| 400 | Email/Username already exists |
| 400 | Validation failed |
| 401 | Unauthenticated / Wrong password |
| 403 | Access denied / User banned |
| 404 | User not found |
| 500 | Internal server error |

---

## Ghi chú

- **Access Token:** 1 giờ
- **Refresh Token:** 7 ngày
- **PATCH:** Chỉ cập nhật field được gửi, field null giữ nguyên
