# API Message

Base URL: `/api/v1/messages`

## Endpoints

### 1. Gui tin nhan
```http
POST /messages
```

Request body cho `TEXT` (`application/json`):
```json
{
  "conversation_id": "uuid-conversation",
  "content": "Noi dung tin nhan",
  "type": "TEXT"
}
```

Gui `IMAGE` qua `multipart/form-data`:

- Part `message`
```json
{
  "conversation_id": "uuid-conversation",
  "type": "IMAGE"
}
```
- Part `file`: file anh thuc te (`image/jpeg`, `image/png`, `image/gif`, `image/webp`)

`type` hien ho tro: `TEXT`, `IMAGE`, `FILE`, `SYSTEM`.

Voi `IMAGE`, backend se upload file len cloud va luu URL vao `content`.
Sau khi luu thanh cong, backend cung broadcast realtime message moi len topic `/topic/conversation/{conversation_id}`.

Response:
```json
{
  "code": 200,
  "data": {
    "id": "uuid-message",
    "conversation_id": "uuid-conversation",
    "sender_id": "uuid-sender",
    "type": "TEXT",
    "content": "Noi dung tin nhan",
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

### 2. Lay tin nhan trong conversation
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
      "content": "Tin nhan 1",
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

### 3. Lay tin nhan voi phan trang
```http
GET /messages/conversation/{conversationId}/paged?page=0&size=20
```

Query parameters:
- `page`: so trang, mac dinh `0`
- `size`: so ban ghi, mac dinh `20`

### 4. Danh dau mot tin nhan da doc
```http
PUT /messages/{messageId}/read
```

Response:
```json
{
  "code": 200,
  "data": "Message marked as read"
}
```

### 5. Danh dau toan bo tin nhan trong conversation da doc
```http
PUT /messages/conversation/{conversationId}/read-all
```

Response:
```json
{
  "code": 200,
  "data": "All messages marked as read"
}
```

### 6. Xoa tin nhan phia ca nhan
```http
DELETE /messages/{messageId}
```

Luu y:
- Day la xoa phia ca nhan, duoc luu qua bang `message_deletions`.
- Ban ghi trong `messages` khong bi xoa cung.
- Tin nhan da xoa voi user hien tai se khong con xuat hien trong danh sach cua user do.

Response:
```json
{
  "code": 200,
  "data": "Message deleted"
}
```

### 7. Dem so tin nhan chua doc
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

### 8. Lay tin nhan moi nhat
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
    "content": "Tin nhan moi nhat",
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

## Ghi chu nghiep vu

- `is_read` trong response duoc suy ra tu bang `message_receipts`, khong nam truc tiep tren bang `messages`.
- Xoa tin nhan phia ca nhan duoc luu o bang `message_deletions`.
- Schema hien co san cac cot recall/edit tren `messages`, nhung tai lieu nay moi phan anh trang thai response hien tai cua backend.

## Error codes

| Code | Message |
|------|---------|
| 400 | Message content cannot be empty |
| 400 | Image message requires an image file |
| 400 | Message image must be a JPG, PNG, GIF, or WEBP image |
| 400 | Message image size must not exceed 10MB |
| 404 | Message not found |
| 404 | Conversation not found |
| 403 | You are not a member of this conversation |
