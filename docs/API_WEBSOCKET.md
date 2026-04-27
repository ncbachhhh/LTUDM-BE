# WebSocket API - Realtime Chat

Base URL: `/api/v1`

WebSocket endpoint:
- Native WebSocket: `ws://localhost:8080/api/v1/ws`
- SockJS endpoint: `http://localhost:8080/api/v1/ws`

## 1. Ket noi WebSocket

### Cach 1. `@stomp/stompjs` voi native WebSocket

```javascript
import { Client } from '@stomp/stompjs';

const client = new Client({
  brokerURL: 'ws://localhost:8080/api/v1/ws',
  connectHeaders: {
    Authorization: `Bearer ${accessToken}`,
  },
});
```

### Cach 2. `@stomp/stompjs` + `sockjs-client`

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

Header bat buoc:

| Key | Value |
|-----|-------|
| Authorization | Bearer {accessToken} |

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

### 3.2 Gui anh

Anh khong duoc day truc tiep trong STOMP frame. Client can:

1. Goi REST `POST /api/v1/messages` voi `multipart/form-data`
2. Backend upload anh len cloud, luu message `type = IMAGE`
3. Backend broadcast `MessageResponse` moi len `/topic/conversation/{conversationId}`

Request:
- Part `message`
```json
{
  "conversation_id": "uuid-conversation",
  "type": "IMAGE"
}
```
- Part `file`: file anh thuc te

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

Luu y:
- `userId` cua typing event duoc backend lay tu JWT da xac thuc, khong tin tu payload client.

## 4. Message format

Message duoc nhan ve tu `/topic/conversation/{conversationId}`:

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

Neu la anh:
- `type = IMAGE`
- `content` se la public URL cua anh sau khi upload thanh cong

Typing event:

```json
{
  "userId": "uuid",
  "displayName": "John",
  "isTyping": true
}
```

## 5. Ghi chu

- STOMP app prefix la `/app`.
- Broker prefix la `/topic` va `/queue`.
- Token duoc xac thuc tai frame `CONNECT`.
- Neu token het han, client phai reconnect voi token moi.
- Chat realtime hien tai ho tro day du cho text qua STOMP va cho anh theo mo hinh REST upload + STOMP broadcast.
