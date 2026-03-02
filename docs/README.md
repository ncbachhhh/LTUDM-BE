# 📖 LTUDM Backend Documentation

> **Project:** LTUDM Backend  
> **Version:** 1.0  
> **Last Updated:** 2026-03-02

---

## 📋 Mục lục tài liệu

| File | Mô tả |
|------|-------|
| [API_USER.md](./API_USER.md) | Tài liệu API cho module User (Authentication, User, Admin) |

---

## 🏗️ Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────────────┐
│                        Client (Frontend)                     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Controller Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │AuthController│  │UserController│  │AdminController│       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       Service Layer                          │
│  ┌──────────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │AuthenticationSvc │  │ UserService  │  │ AdminService │   │
│  └──────────────────┘  └──────────────┘  └──────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Repository Layer                        │
│  ┌──────────────────┐  ┌─────────────────────────────┐      │
│  │  UserRepository  │  │ InvalidatedTokenRepository  │      │
│  └──────────────────┘  └─────────────────────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                         Database                             │
│                        (MySQL/PostgreSQL)                    │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 Cấu trúc thư mục

```
src/main/java/com/ncbachhhh/LTUDM/
├── config/                 # Cấu hình (Security, Bean...)
│   ├── SecurityConfig.java
│   └── UserSecurity.java
├── controller/             # REST Controllers
│   ├── AuthenticationController.java
│   ├── UserController.java
│   └── AdminController.java
├── dto/                    # Data Transfer Objects
│   ├── request/            # Request DTOs
│   │   ├── AuthenticationRequest.java
│   │   ├── UserRegisterRequest.java
│   │   ├── UserUpdateRequest.java
│   │   ├── ChangePasswordRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   ├── LogoutRequest.java
│   │   └── IntrospectRequest.java
│   └── response/           # Response DTOs
│       ├── ApiResponse.java
│       ├── UserResponse.java
│       ├── AuthenticationResponse.java
│       └── IntrospectResponse.java
├── entity/                 # JPA Entities
│   ├── User/
│   │   ├── User.java
│   │   └── UserRole.java
│   └── InvalidatedToken.java
├── exception/              # Exception Handling
│   ├── AppException.java
│   ├── ErrorCode.java
│   └── GlobalExceptionHandler.java
├── mapper/                 # MapStruct Mappers
│   └── UserMapper.java
├── repository/             # JPA Repositories
│   ├── UserRepository.java
│   └── InvalidatedTokenRepository.java
├── service/                # Business Logic
│   ├── AuthenticationService.java
│   ├── UserService.java
│   └── AdminService.java
└── LtudmApplication.java   # Main Application
```

---

## 🔐 Bảo mật

### Authentication Flow

```
┌────────┐                                           ┌────────┐
│ Client │                                           │ Server │
└───┬────┘                                           └───┬────┘
    │                                                    │
    │  1. POST /auth/login {email, password}             │
    │ ──────────────────────────────────────────────────>│
    │                                                    │
    │  2. Return {accessToken, refreshToken}             │
    │ <──────────────────────────────────────────────────│
    │                                                    │
    │  3. GET /users/me (Authorization: Bearer token)    │
    │ ──────────────────────────────────────────────────>│
    │                                                    │
    │  4. Return user data                               │
    │ <──────────────────────────────────────────────────│
    │                                                    │
    │  5. POST /auth/refresh {refreshToken}              │
    │ ──────────────────────────────────────────────────>│
    │                                                    │
    │  6. Return new {accessToken, refreshToken}         │
    │ <──────────────────────────────────────────────────│
    │                                                    │
    │  7. POST /auth/logout {token}                      │
    │ ──────────────────────────────────────────────────>│
    │                                                    │
    │  8. Token invalidated                              │
    │ <──────────────────────────────────────────────────│
```

### Authorization Rules

| Endpoint | Role Required | Additional Check |
|----------|---------------|------------------|
| `POST /auth/*` | None | Public |
| `GET /users/me` | Authenticated | - |
| `GET /users/{userId}` | Authenticated | Owner hoặc Admin |
| `PATCH /users/{userId}` | Authenticated | Owner hoặc Admin |
| `POST /users/me/change-password` | Authenticated | - |
| `POST /admin/*` | ADMIN | - |
| `PATCH /admin/*` | ADMIN | - |

---

## 🗄️ Database Schema

### Table: users

| Column | Type | Constraints |
|--------|------|-------------|
| id | VARCHAR(36) | PRIMARY KEY |
| email | VARCHAR(100) | NOT NULL, UNIQUE |
| username | VARCHAR(50) | NOT NULL, UNIQUE |
| password_hash | VARCHAR(255) | NOT NULL |
| display_name | VARCHAR(100) | - |
| avatar_url | VARCHAR(500) | - |
| created_at | DATETIME | NOT NULL |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'USER' |
| is_active | BOOLEAN | NOT NULL, DEFAULT TRUE |

### Table: invalidated_tokens

| Column | Type | Constraints |
|--------|------|-------------|
| id | VARCHAR(36) | PRIMARY KEY |
| expiry_time | DATETIME | NOT NULL |

---

## ⚙️ Configuration

### application.yaml

```yaml
jwt:
  secret: your-secret-key-min-32-characters
  access-token-expiration: 3600      # 1 hour
  refresh-token-expiration: 604800   # 7 days
```

---

## 🚀 Quick Start

```bash
# Clone repository
git clone <repository-url>

# Navigate to project
cd LTUDM-Backend

# Run with Maven
./mvnw spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/LTUDM-0.0.1-SNAPSHOT.jar
```

---

## 📝 Conventions

### API Response Format

**Success:**
```json
{
    "code": 200,
    "message": "Optional message",
    "data": { ... }
}
```

**Error:**
```json
{
    "code": 400,
    "message": "Error description"
}
```

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Entity | PascalCase | `User`, `InvalidatedToken` |
| DTO | PascalCase + Suffix | `UserResponse`, `UserRegisterRequest` |
| Controller | PascalCase + Controller | `UserController` |
| Service | PascalCase + Service | `UserService` |
| Repository | PascalCase + Repository | `UserRepository` |

### HTTP Methods

| Method | Usage |
|--------|-------|
| GET | Lấy dữ liệu |
| POST | Tạo mới, Action |
| PATCH | Cập nhật partial |
| PUT | Cập nhật toàn bộ |
| DELETE | Xóa |
