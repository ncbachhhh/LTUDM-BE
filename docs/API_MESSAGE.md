# Tài Liệu API - Message

Base URL:

```text
http://localhost:8080/api/v1
```

## Endpoint

### 1. Gửi tin nhắn

```http
POST /messages
```

Request body cho `TEXT` (`application/json`):

```json
{
  "conversation_id": "uuid-conversation",
  "content": "Nội dung tin nhắn",
  "type": "TEXT"
}
```

Gửi `IMAGE` qua `multipart/form-data`:

Part `message`:

```json
{
  "conversation_id": "uuid-conversation",
  "type": "IMAGE"
}
```

Part `file`: file ảnh thực tế (`image/jpeg`, `image/png`, `image/gif`, `image/webp`).

`type` hiện hỗ trợ: `TEXT`, `IMAGE`, `FILE`, `SYSTEM`.

Với `IMAGE`, backend sẽ upload file lên cloud và lưu URL vào `content`.
Sau khi lưu thành công, backend cũng broadcast realtime message mới lên topic `/topic/conversation/{conversation_id}`.

Response:

```json
{
  "code": 200,
  "data": {
    "id": "uuid-message",
    "conversation_id": "uuid-conversation",
    "sender_id": "uuid-sender",
    "type": "TEXT",
    "content": "Nội dung tin nhắn",
    "created_at": "2026-03-12T10:30:00",
    "is_read": false,
    "is_edited": false,
    "edited_at": null,
    "is_recalled": false,
    "recalled_at": null,
    "recalled_by": null
  }
}
```

### 2. Lấy tin nhắn trong conversation

```http
GET /messages/conversation/{conversationId}
```

Response:

```json
{
  "code": 200,
  "data": [
    {
      "id": "uuid-message-1",
      "conversation_id": "uuid-conversation",
      "sender_id": "uuid-sender",
      "type": "TEXT",
      "content": "Tin nhắn 1",
      "created_at": "2026-03-12T10:30:00",
      "is_read": true,
      "is_edited": false,
      "edited_at": null,
      "is_recalled": false,
      "recalled_at": null,
      "recalled_by": null
    }
  ]
}
```

### 3. Lấy tin nhắn với phân trang

```http
GET /messages/conversation/{conversationId}/paged?page=0&size=20
```

Query parameters:

- `page`: số trang, mặc định `0`.
- `size`: số bản ghi, mặc định `20`.

### 4. Đánh dấu một tin nhắn đã đọc

```http
PUT /messages/{messageId}/read
```

Response:

```json
{
  "code": 200,
  "data": "Đã đánh dấu tin nhắn là đã đọc."
}
```

### 5. Đánh dấu toàn bộ tin nhắn trong conversation đã đọc

```http
PUT /messages/conversation/{conversationId}/read-all
```

Response:

```json
{
  "code": 200,
  "data": "Đã đánh dấu toàn bộ tin nhắn là đã đọc."
}
```

### 6. Xóa tin nhắn phía cá nhân

```http
DELETE /messages/{messageId}
```

Lưu ý:

- Đây là xóa phía cá nhân, được lưu qua bảng `message_deletions`.
- Bản ghi trong `messages` không bị xóa cứng.
- Tin nhắn đã xóa với user hiện tại sẽ không còn xuất hiện trong danh sách của user đó.

Response:

```json
{
  "code": 200,
  "data": "Đã xóa tin nhắn."
}
```

### 7. Đếm số tin nhắn chưa đọc

```http
GET /messages/conversation/{conversationId}/unread-count
```

Response:

```json
{
  "code": 200,
  "data": 5
}
```

### 8. Lấy tin nhắn mới nhất

```http
GET /messages/conversation/{conversationId}/latest
```

Response:

```json
{
  "code": 200,
  "data": {
    "id": "uuid-message",
    "conversation_id": "uuid-conversation",
    "sender_id": "uuid-sender",
    "type": "TEXT",
    "content": "Tin nhắn mới nhất",
    "created_at": "2026-03-12T10:35:00",
    "is_read": false,
    "is_edited": false,
    "edited_at": null,
    "is_recalled": false,
    "recalled_at": null,
    "recalled_by": null
  }
}
```

## Ghi chú nghiệp vụ

- `is_read` trong response được suy ra từ bảng `message_receipts`, không nằm trực tiếp trên bảng `messages`.
- Xóa tin nhắn phía cá nhân được lưu ở bảng `message_deletions`.
- Schema hiện có sẵn các cột recall/edit trên `messages`, nhưng tài liệu này mới phản ánh trạng thái response hiện tại của backend.

## Mã lỗi

| Code | Message |
|------|---------|
| 400 | Nội dung tin nhắn không được để trống |
| 400 | Tin nhắn ảnh cần có file ảnh |
| 400 | Ảnh tin nhắn phải là JPG, PNG, GIF hoặc WEBP |
| 400 | Dung lượng ảnh tin nhắn không được vượt quá 10MB |
| 404 | Không tìm thấy tin nhắn |
| 404 | Không tìm thấy đoạn chat |
| 403 | Bạn không phải thành viên của đoạn chat này |
