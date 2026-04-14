# LTUDM Backend Documentation

Last updated: 2026-04-09

## Tài liệu

| File | Mô tả |
|------|-------|
| [API_USER.md](./API_USER.md) | API cho authentication và user |
| [API_MESSAGE.md](./API_MESSAGE.md) | API cho message |
| [API_CONVERSATION.md](./API_CONVERSATION.md) | API cho táº¡o vÃ  quáº£n lÃ½ Ä‘oáº¡n chat |
| [API_WEBSOCKET.md](./API_WEBSOCKET.md) | WebSocket cho realtime chat |

## Ghi chú trạng thái hiện tại

- `AdminController` vẫn tồn tại trong code nhưng toàn bộ endpoint admin đã bị comment out.
- JSON request/response đang dùng snake_case ở lớp DTO.
- Message read status được suy ra từ `message_receipts`.
- Message delete phía cá nhân được lưu ở `message_deletions`.
- `MessageType` hiện hỗ trợ: `TEXT`, `IMAGE`, `FILE`, `SYSTEM`.

## Authorization rules

| Endpoint | Role required | Additional check |
|----------|---------------|------------------|
| `POST /auth/*` | Public | - |
| `GET /users/me` | Authenticated | - |
| `GET /users/{userId}` | Authenticated | Owner hoặc Admin |
| `PATCH /users/{userId}` | Authenticated | Owner hoặc Admin |
| `POST /users/me/change-password` | Authenticated | - |
| `/admin/*` | Disabled | Các endpoint hiện không active |

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
