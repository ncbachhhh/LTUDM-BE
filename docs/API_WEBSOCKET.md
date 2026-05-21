# Tai lieu WebSocket - Realtime Chat

Base URL:

```text
http://localhost:8080/api/v1
```

WebSocket endpoint:

- Native WebSocket: `ws://localhost:8080/api/v1/ws`
- SockJS endpoint: `http://localhost:8080/api/v1/ws`

## 1. Ket noi WebSocket

Header bat buoc khi CONNECT:

| Key | Value |
|-----|-------|
| Authorization | Bearer {accessToken} |

Vi du `@stomp/stompjs` voi native WebSocket:

```javascript
import { Client } from '@stomp/stompjs';

const client = new Client({
  brokerURL: 'ws://localhost:8080/api/v1/ws',
  connectHeaders: {
    Authorization: `Bearer ${accessToken}`,
  },
});
```

## 2. Subscribe

### 2.1 Nhan tin nhan trong conversation

```text
/topic/conversation/{conversationId}
```

### 2.2 Nhan thong bao da doc

```text
/topic/conversation/{conversationId}/read
```

### 2.3 Nhan typing indicator

```text
/topic/conversation/{conversationId}/typing
```

### 2.4 Nhan conversation preview cua user hien tai

```text
/user/queue/conversations
```

## 3. Send

### 3.1 Gui tin nhan text qua STOMP

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

### 3.2 Gui anh/file

Anh va file binary khong duoc day truc tiep trong STOMP frame. Client can:

1. Goi REST `POST /api/v1/messages` voi `multipart/form-data`.
2. Backend upload file len R2, luu message `type = IMAGE` hoac `type = FILE`.
3. Backend broadcast `MessageResponse` moi len `/topic/conversation/{conversationId}`.
4. Backend push conversation preview den `/user/queue/conversations`.

Part `message` khi gui anh:

```json
{
  "conversation_id": "uuid-conversation",
  "type": "IMAGE"
}
```

Part `message` khi gui file:

```json
{
  "conversation_id": "uuid-conversation",
  "type": "FILE"
}
```

Part `file`: file can upload.

### 3.3 Danh dau da doc

```text
/app/chat/{conversationId}/read
```

### 3.4 Dang go

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

`userId` cua typing event duoc backend lay tu JWT da xac thuc, khong tin tu payload client.

## 4. Message format

Message duoc nhan ve tu `/topic/conversation/{conversationId}`:

```json
{
  "id": "uuid",
  "conversation_id": "uuid",
  "sender_id": "uuid",
  "content": "Hello!",
  "type": "TEXT",
  "attachment": null,
  "created_at": "2026-03-02T10:30:00",
  "is_read": false,
  "is_edited": false,
  "edited_at": null,
  "is_recalled": false,
  "recalled_at": null,
  "recalled_by": null
}
```

Neu la `IMAGE` hoac `FILE`:

- `content` la public URL cua file sau khi upload thanh cong.
- `attachment` chua metadata file.

```json
{
  "id": "uuid",
  "conversation_id": "uuid",
  "sender_id": "uuid",
  "content": "https://public-r2-domain/messages/conversation/sender/files/random.pdf",
  "type": "FILE",
  "attachment": {
    "id": "uuid-attachment",
    "file_url": "https://public-r2-domain/messages/conversation/sender/files/random.pdf",
    "file_name": "document.pdf",
    "mime_type": "application/pdf",
    "file_size": 123456
  },
  "created_at": "2026-03-02T10:30:00",
  "is_read": false,
  "is_edited": false,
  "edited_at": null,
  "is_recalled": false,
  "recalled_at": null,
  "recalled_by": null
}
```

## 5. Ghi chu

- STOMP app prefix la `/app`.
- Broker prefix la `/topic` va `/queue`.
- Token duoc xac thuc tai frame `CONNECT`.
- Neu token het han, client phai reconnect voi token moi.
- Chat realtime ho tro text qua STOMP; anh/file dung REST upload + STOMP broadcast.
