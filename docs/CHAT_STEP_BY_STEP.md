# Hướng Dẫn Chat Cơ Bản Từng Bước

Tài liệu này mô tả cách dùng backend hiện tại để test luồng chat cơ bản:

- Đăng ký hoặc đăng nhập.
- Tạo đoạn chat cá nhân hoặc nhóm.
- Gửi tin nhắn.
- Lấy lịch sử tin nhắn.
- Đánh dấu đã đọc.
- Thêm thành viên vào nhóm.
- Xóa nhóm chat.

## 1. Điều kiện cần

- Backend đang chạy thành công.
- Có ít nhất 2 tài khoản để test chat cá nhân.
- Mọi API chat cần header `Authorization: Bearer <access_token>`.

Base URL mặc định:

```text
http://localhost:8080/api/v1
```

## 2. Luồng sử dụng chuẩn

### Bước 1: Đăng ký tài khoản

```http
POST /auth/register
Content-Type: application/json
```

Body:

```json
{
  "email": "user1@example.com",
  "username": "user1",
  "password": "12345678",
  "display_name": "User One"
}
```

Làm tương tự để tạo thêm:

- `user2@example.com`
- `user3@example.com`

### Bước 2: Đăng nhập để lấy token

```http
POST /auth/login
Content-Type: application/json
```

Body:

```json
{
  "email": "user1@example.com",
  "password": "12345678"
}
```

Response mẫu:

```json
{
  "code": 200,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "isAuthenticated": true
  }
}
```

Cần lưu:

- `accessToken`
- `refreshToken`

Sau đó thêm header cho các request cần đăng nhập:

```http
Authorization: Bearer eyJ...
```

## 3. Tạo chat cá nhân

### Bước 3: Lấy `userId` của người còn lại

Backend hiện chưa có API search user công khai theo username. Để test, cần biết trước `userId` của tài khoản còn lại:

- Lấy từ database.
- Lấy từ response tạo user.
- Gọi `GET /users/me` khi đăng nhập từng tài khoản.

Ví dụ đã có:

- `user1Id`
- `user2Id`

### Bước 4: Tạo conversation cá nhân

Đăng nhập bằng `user1`, sau đó gọi:

```http
POST /conversations
Authorization: Bearer <token_user1>
Content-Type: application/json
```

Body:

```json
{
  "type": "DIRECT",
  "member_ids": ["user2Id"]
}
```

Kết quả:

- Nếu chưa có chat cá nhân giữa `user1` và `user2`, hệ thống tạo mới.
- Nếu đã có, hệ thống trả về conversation cũ.

Response mẫu:

```json
{
  "code": 200,
  "data": {
    "id": "conversationId",
    "type": "DIRECT",
    "title": null,
    "created_by": "user1Id",
    "created_at": "2026-04-14T10:00:00",
    "avatar_url": null,
    "members": [
      {
        "user_id": "user1Id",
        "username": "user1",
        "display_name": "User One",
        "avatar_url": null,
        "role": "OWNER",
        "joined_at": "2026-04-14T10:00:00"
      },
      {
        "user_id": "user2Id",
        "username": "user2",
        "display_name": "User Two",
        "avatar_url": null,
        "role": "MEMBER",
        "joined_at": "2026-04-14T10:00:00"
      }
    ]
  }
}
```

Hãy lưu `conversationId`.

## 4. Gửi và đọc tin nhắn

### Bước 5: Gửi tin nhắn

```http
POST /messages
Authorization: Bearer <token_user1>
Content-Type: application/json
```

Body:

```json
{
  "conversation_id": "conversationId",
  "content": "Xin chào",
  "type": "TEXT"
}
```

### Bước 6: Lấy danh sách tin nhắn

```http
GET /messages/conversation/conversationId
Authorization: Bearer <token_user1>
```

Hoặc phân trang:

```http
GET /messages/conversation/conversationId/paged?page=0&size=20
Authorization: Bearer <token_user1>
```

### Bước 7: Lấy tin nhắn mới nhất

```http
GET /messages/conversation/conversationId/latest
Authorization: Bearer <token_user1>
```

### Bước 8: Đếm số tin nhắn chưa đọc

```http
GET /messages/conversation/conversationId/unread-count
Authorization: Bearer <token_user2>
```

### Bước 9: Đánh dấu một tin nhắn đã đọc

```http
PUT /messages/{messageId}/read
Authorization: Bearer <token_user2>
```

### Bước 10: Đánh dấu toàn bộ conversation đã đọc

```http
PUT /messages/conversation/conversationId/read-all
Authorization: Bearer <token_user2>
```

### Bước 11: Xóa mềm tin nhắn phía cá nhân

```http
DELETE /messages/{messageId}
Authorization: Bearer <token_user1>
```

Ý nghĩa:

- Chỉ ẩn tin nhắn với user hiện tại.
- Không xóa cứng khỏi bảng `messages`.

## 5. Tạo chat nhóm

### Bước 12: Tạo nhóm chat

Đăng nhập bằng user tạo nhóm:

```http
POST /conversations
Authorization: Bearer <token_user1>
Content-Type: application/json
```

Body:

```json
{
  "type": "GROUP",
  "title": "Nhóm dự án",
  "avatar_url": "https://example.com/group.png",
  "member_ids": ["user2Id", "user3Id"]
}
```

Kết quả:

- `user1` tự động là `OWNER`.
- `user2`, `user3` là `MEMBER`.

## 6. Quản lý nhóm chat

### Bước 13: Thêm thành viên vào nhóm

Chỉ `OWNER` hoặc `ADMIN` mới làm được:

```http
POST /conversations/{conversationId}/members
Authorization: Bearer <token_owner_or_admin>
Content-Type: application/json
```

Body:

```json
{
  "member_ids": ["user4Id", "user5Id"]
}
```

### Bước 14: Xóa nhóm chat

Chỉ `OWNER` hoặc `ADMIN` mới làm được:

```http
DELETE /conversations/{conversationId}
Authorization: Bearer <token_owner_or_admin>
```

Khi xóa nhóm:

- Conversation bị xóa.
- Conversation members bị xóa.
- Message trong group bị xóa.
- Receipt và deletion liên quan cũng bị xóa.

## 7. Cách test nhanh bằng Postman

Thứ tự collection nên chạy:

1. `POST /auth/register` cho `user1`.
2. `POST /auth/register` cho `user2`.
3. `POST /auth/login` cho `user1`.
4. `POST /auth/login` cho `user2`.
5. `POST /conversations` với `type=DIRECT`.
6. `POST /messages`.
7. `GET /messages/conversation/{conversationId}`.
8. `PUT /messages/conversation/{conversationId}/read-all`.
9. `GET /messages/conversation/{conversationId}/unread-count`.

## 8. Kết luận hiện trạng

Backend hiện đã đủ cho chat cơ bản ở mức API:

- Tạo chat cá nhân.
- Tạo chat nhóm.
- Gửi tin nhắn.
- Lấy tin nhắn.
- Đánh dấu đã đọc.
- Thêm thành viên nhóm.
- Xóa nhóm.

## 9. Lưu ý quan trọng trước khi đưa vào production

Hiện còn một điểm nghiệp vụ chưa chặt:

- `MessageService` chưa kiểm tra người gửi có phải là thành viên của `conversation` hay không trước khi gửi tin nhắn.

Điều này có nghĩa là:

- Backend hiện tại đủ để test và demo luồng chat cơ bản.
- Nhưng chưa an toàn để xem là bản production-ready.

Nên bổ sung tiếp:

- Kiểm tra conversation có tồn tại.
- Kiểm tra sender là thành viên của conversation.
- Kiểm tra user chỉ được đọc hoặc đánh dấu read trong conversation mà họ tham gia.

## 10. Tài liệu liên quan

- [API_CONVERSATION.md](./API_CONVERSATION.md)
- [API_MESSAGE.md](./API_MESSAGE.md)
- [API_USER.md](./API_USER.md)
