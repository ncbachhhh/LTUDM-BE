# API Message

Base URL: `/api/v1/messages`

## Endpoints

### 1. Gửi tin nhắn
```
POST /messages
```

**Request Body:**
```json
{
  "conversation_id": "uuid-conversation",
  "content": "Nội dung tin nhắn",
  "type": "TEXT"  // TEXT, IMAGE, VIDEO, FILE, SYSTEM (mặc định: TEXT)
}
```

**Response:**
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
    "is_read": false
  }
}
```

---

### 2. Lấy tin nhắn trong conversation
```
GET /messages/conversation/{conversationId}
```

**Response:**
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
      "is_read": true
    },
    {
      "id": "uuid-message-2",
      "conversation_id": "uuid-conversation",
      "sender_id": "uuid-other",
      "type": "TEXT",
      "content": "Tin nhắn 2",
      "created_at": "2026-03-12T10:31:00",
      "is_read": false
    }
  ]
}
```

---

### 3. Lấy tin nhắn với phân trang
```
GET /messages/conversation/{conversationId}/paged?page=0&size=20
```

**Query Parameters:**
- `page`: Số trang (mặc định: 0)
- `size`: Số tin nhắn/trang (mặc định: 20)

---

### 4. Đánh dấu tin nhắn đã đọc
```
PUT /messages/{messageId}/read
```

**Response:**
```json
{
  "code": 200,
  "data": "Message marked as read"
}
```

---

### 5. Đánh dấu tất cả tin nhắn đã đọc
```
PUT /messages/conversation/{conversationId}/read-all
```

**Response:**
```json
{
  "code": 200,
  "data": "All messages marked as read"
}
```

---

### 6. Xóa tin nhắn
```
DELETE /messages/{messageId}
```

**Note:** Chỉ người gửi mới có thể xóa tin nhắn của mình (soft delete)

**Response:**
```json
{
  "code": 200,
  "data": "Message deleted"
}
```

---

### 7. Đếm tin nhắn chưa đọc
```
GET /messages/conversation/{conversationId}/unread-count
```

**Response:**
```json
{
  "code": 200,
  "data": 5
}
```

---

### 8. Lấy tin nhắn mới nhất
```
GET /messages/conversation/{conversationId}/latest
```

**Response:**
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
    "is_read": false
  }
}
```

---

## Error Codes

| Code | Message |
|------|---------|
| 404 | Message not found |
| 404 | Conversation not found |
| 403 | You are not a member of this conversation |
| 400 | Message content cannot be empty |
| 403 | Access denied (khi xóa tin nhắn của người khác) |
