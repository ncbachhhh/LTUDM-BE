# LTUDM Backend Documentation

Last updated: 2026-04-18

## Tai lieu

| File | Mo ta |
|------|-------|
| [API_USER.md](./API_USER.md) | API cho authentication, user va upload avatar |
| [API_FILE_UPLOAD.md](./API_FILE_UPLOAD.md) | Co che upload file len Cloudflare R2 va cach tai su dung trong code |
| [API_MESSAGE.md](./API_MESSAGE.md) | API cho message |
| [API_CONVERSATION.md](./API_CONVERSATION.md) | API cho tao va quan ly doan chat |
| [API_WEBSOCKET.md](./API_WEBSOCKET.md) | WebSocket cho realtime chat |

## Ghi chu trang thai hien tai

- `AdminController` van ton tai trong code nhung toan bo endpoint admin da bi comment out.
- JSON request/response dang dung snake_case o lop DTO.
- Message read status duoc suy ra tu `message_receipts`.
- Message delete phia ca nhan duoc luu o `message_deletions`.
- `MessageType` hien ho tro: `TEXT`, `IMAGE`, `FILE`, `SYSTEM`.
- Upload avatar dang dung Cloudflare R2 thong qua `R2StorageService`.

## Authorization rules

| Endpoint | Role required | Additional check |
|----------|---------------|------------------|
| `POST /auth/*` | Public | - |
| `GET /users/me` | Authenticated | - |
| `GET /users/{userId}` | Authenticated | Owner hoac Admin |
| `PATCH /users/{userId}` | Authenticated | Owner hoac Admin |
| `PATCH /users/me/avatar` | Authenticated | - |
| `POST /users/me/change-password` | Authenticated | - |
| `/admin/*` | Disabled | Cac endpoint hien khong active |

## Database notes

### users

| Column | Type |
|--------|------|
| id | CHAR(36) |
| email | VARCHAR(255) |
| username | VARCHAR(50) |
| password_hash | VARCHAR(255) |
| display_name | VARCHAR(100) |
| avatar_url | VARCHAR(500) |
| created_at | DATETIME |
| role | ENUM('ADMIN', 'USER') |
| is_active | BOOLEAN |

### messages

| Column | Type |
|--------|------|
| id | CHAR(36) |
| conversation_id | CHAR(36) |
| sender_id | CHAR(36) |
| type | ENUM('TEXT', 'FILE', 'IMAGE', 'SYSTEM') |
| content | TEXT |
| created_at | DATETIME |
| is_edited | BOOLEAN |
| edited_at | DATETIME |
| is_recalled | BOOLEAN |
| recalled_at | DATETIME |
| recalled_by | CHAR(36) |

### message_receipts

| Column | Type |
|--------|------|
| message_id | CHAR(36) |
| user_id | CHAR(36) |
| seen_at | DATETIME |

### message_deletions

| Column | Type |
|--------|------|
| message_id | CHAR(36) |
| user_id | CHAR(36) |
| deleted_at | DATETIME |
