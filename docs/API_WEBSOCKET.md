# WebSocket API - Realtime Chat

WebSocket URL: `ws://localhost:8080/api/v1/ws`

## 1. Kết nối WebSocket

```javascript
const socket = new SockJS('http://localhost:8080/api/v1/ws');
const stompClient = Stomp.over(socket);

stompClient.connect(
  { Authorization: 'Bearer ' + accessToken },
  onConnected,
  onError
);
```

Header bắt buộc:

| Key | Value |
|-----|-------|
| Authorization | Bearer {accessToken} |

## 2. Subscribe

### 2.1 Nhận tin nhắn conversation
```text
/topic/conversation/{conversationId}
```

### 2.2 Nhận thông báo đã đọc
```text
/topic/conversation/{conversationId}/read
```

### 2.3 Typing indicator
```text
/topic/conversation/{conversationId}/typing
```

## 3. Send

### 3.1 Gửi tin nhắn
```text
/app/chat/{conversationId}
```

Payload:
```json
{
  "content": "Hello!",
  "type": "TEXT"
}
```

### 3.2 Đánh dấu đã đọc
```text
/app/chat/{conversationId}/read
```

### 3.3 Đang gõ
```text
/app/chat/{conversationId}/typing
```

Payload:
```json
{
  "userId": "uuid",
  "displayName": "John",
  "isTyping": true
}
```

## 4. Message format

Tin nhắn gửi đi:

| Field | Type | Description |
|-------|------|-------------|
| content | string | Nội dung tin nhắn |
| type | string | `TEXT`, `IMAGE`, `FILE`, `SYSTEM` |

Tin nhắn nhận về:
```json
{
  "id": "uuid",
  "conversation_id": "uuid",
  "sender_id": "uuid",
  "content": "Hello!",
  "type": "TEXT",
  "created_at": "2026-03-02T10:30:00",
  "is_read": false,
  "is_edited": false,
  "edited_at": null,
  "is_recalled": false,
  "recalled_at": null,
  "recalled_by": null
}
```

## 5. Ghi chú

- WebSocket dùng STOMP protocol.
- Token được xác thực khi CONNECT.
- Nếu token hết hạn, client cần reconnect với token mới.
