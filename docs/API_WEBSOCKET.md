# Tài Liệu WebSocket - Realtime Chat

Base URL:

```text
http://localhost:8080/api/v1
```

WebSocket endpoint:

- Native WebSocket: `ws://localhost:8080/api/v1/ws`
- SockJS endpoint: `http://localhost:8080/api/v1/ws`

## 1. Kết nối WebSocket

### Cách 1. `@stomp/stompjs` với native WebSocket

```javascript
import { Client } from '@stomp/stompjs';

const client = new Client({
  brokerURL: 'ws://localhost:8080/api/v1/ws',
  connectHeaders: {
    Authorization: `Bearer ${accessToken}`,
  },
});
```

### Cách 2. `@stomp/stompjs` kết hợp `sockjs-client`

```javascript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/api/v1/ws'),
  connectHeaders: {
    Authorization: `Bearer ${accessToken}`,
  },
});
```

Header bắt buộc:

| Key | Value |
|-----|-------|
| Authorization | Bearer {accessToken} |

## 2. Subscribe

### 2.1 Nhận tin nhắn trong conversation

```text
/topic/conversation/{conversationId}
```

### 2.2 Nhận thông báo đã đọc

```text
/topic/conversation/{conversationId}/read
```

### 2.3 Nhận typing indicator

```text
/topic/conversation/{conversationId}/typing
```

## 3. Send

### 3.1 Gửi tin nhắn text qua STOMP

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

### 3.2 Gửi ảnh

Ảnh không được đẩy trực tiếp trong STOMP frame. Client cần:

1. Gọi REST `POST /api/v1/messages` với `multipart/form-data`.
2. Backend upload ảnh lên cloud, lưu message `type = IMAGE`.
3. Backend broadcast `MessageResponse` mới lên `/topic/conversation/{conversationId}`.

Request:

Part `message`:

```json
{
  "conversation_id": "uuid-conversation",
  "type": "IMAGE"
}
```

Part `file`: file ảnh thực tế.

### 3.3 Đánh dấu đã đọc

```text
/app/chat/{conversationId}/read
```

### 3.4 Đang gõ

```text
/app/chat/{conversationId}/typing
```

Payload:

```json
{
  "displayName": "John",
  "isTyping": true
}
```

Lưu ý:

- `userId` của typing event được backend lấy từ JWT đã xác thực, không tin từ payload client.

## 4. Message format

Message được nhận về từ `/topic/conversation/{conversationId}`:

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

Nếu là ảnh:

- `type = IMAGE`
- `content` là public URL của ảnh sau khi upload thành công.

Typing event:

```json
{
  "userId": "uuid",
  "displayName": "John",
  "isTyping": true
}
```

## 5. Ghi chú

- STOMP app prefix là `/app`.
- Broker prefix là `/topic` và `/queue`.
- Token được xác thực tại frame `CONNECT`.
- Nếu token hết hạn, client phải reconnect với token mới.
- Chat realtime hiện hỗ trợ đầy đủ cho text qua STOMP và cho ảnh theo mô hình REST upload + STOMP broadcast.
