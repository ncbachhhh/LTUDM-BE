# Tài Liệu LTUDM Backend

Cập nhật lần cuối: 2026-05-13

## Danh sách tài liệu

| File | Mô tả |
|------|------|
| [API_USER.md](./API_USER.md) | API xác thực, user và upload avatar |
| [API_FORGOT_PASSWORD.md](./API_FORGOT_PASSWORD.md) | API quên mật khẩu bằng OTP qua email |
| [API_FILE_UPLOAD.md](./API_FILE_UPLOAD.md) | Cơ chế upload file lên Cloudflare R2 và cách tái sử dụng trong code |
| [API_MESSAGE.md](./API_MESSAGE.md) | API tin nhắn |
| [API_CONVERSATION.md](./API_CONVERSATION.md) | API tạo và quản lý đoạn chat |
| [API_FRIENDSHIP.md](./API_FRIENDSHIP.md) | API ket ban, incoming/outgoing request va rule chi ban be moi chat |
| [API_WEBSOCKET.md](./API_WEBSOCKET.md) | WebSocket cho realtime chat |
| [CHAT_STEP_BY_STEP.md](./CHAT_STEP_BY_STEP.md) | Hướng dẫn test luồng chat cơ bản từng bước |

## Ghi chú trạng thái hiện tại

- `AdminController` vẫn tồn tại trong code nhưng toàn bộ endpoint admin đã bị comment out.
- JSON request/response đang dùng snake_case ở lớp DTO.
- Message read status được suy ra từ `message_receipts`.
- Message delete phía cá nhân được lưu ở `message_deletions`.
- `MessageType` hiện hỗ trợ: `TEXT`, `IMAGE`, `FILE`, `SYSTEM`.
- Upload avatar đang dùng Cloudflare R2 thông qua `R2StorageService`.
- Quên mật khẩu dùng OTP gửi qua email và Redis để lưu OTP, cooldown, attempts, reset token.

## Quy tắc phân quyền

| Endpoint | Quyền yêu cầu | Kiểm tra thêm |
|----------|---------------|---------------|
| `POST /auth/*` | Public | - |
| `GET /users/me` | Đã đăng nhập | - |
| `PATCH /users/{userId}` | Đã đăng nhập | Chính chủ hoặc admin |
| `PATCH /users/me/avatar` | Đã đăng nhập | - |
| `POST /users/me/change-password` | Đã đăng nhập | - |
| `/admin/*` | Đã tắt | Các endpoint hiện không active |

## Ghi chú database

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
