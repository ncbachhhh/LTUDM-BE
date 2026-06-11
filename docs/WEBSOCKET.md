# WebSocket and STOMP

## Endpoint

SockJS/STOMP endpoint:

```text
/api/v1/ws
```

CONNECT header:

```text
Authorization: Bearer <access_token>
```

Token duoc verify trong `WebSocketAuthInterceptor`:

- Header phai co Bearer token.
- JWT phai verify duoc bang `jwt.secret`.
- Token chua expired.
- Token chua bi revoke trong `invalidated_tokens`.
- Principal cua connection la `userId` tu JWT subject.

## Authorization

Interceptor chi enforce conversation access cho destination match:

```text
/app/chat/{conversationId}
/app/chat/{conversationId}/...
/topic/conversation/{conversationId}
/topic/conversation/{conversationId}/...
```

Khi SUBSCRIBE hoac SEND den destination tren:

1. User phai authenticated.
2. User phai la member cua conversation.
3. Voi SEND direct conversation, relationship phai accepted va khong block.

## Subscribe Destinations

### Conversation Messages

```text
/topic/conversation/{conversationId}
```

Nhan `MessageResponse` moi khi REST multipart hoac STOMP send message thanh cong.
Endpoint nay cung nhan `MessageResponse` cap nhat khi tin nhan bi recall, ghim hoac bo ghim.

### Conversation Read Event

```text
/topic/conversation/{conversationId}/read
```

Nhan `MessageReadEventResponse` khi user mark all read qua STOMP hoac REST.

Payload:

```json
{
  "event_type": "MESSAGES_READ",
  "conversation_id": "conversationId",
  "reader": {
    "user_id": "readerUserId",
    "display_name": "Reader Name",
    "nickname": "Optional nickname",
    "seen_at": "2026-06-07T21:00:00"
  },
  "message_ids": ["messageId1", "messageId2"],
  "occurred_at": "2026-06-07T21:00:00"
}
```

Client dung event nay de append `reader` vao `seen_by` cua cac message trong `message_ids`.

### Typing Indicator

```text
/topic/conversation/{conversationId}/typing
```

Payload:

```json
{
  "userId": "senderUserId",
  "displayName": "Optional display name",
  "isTyping": true
}
```

Server override `userId` bang authenticated principal.

### Conversation Preview For Current User

```text
/user/queue/conversations
```

Nhan `ConversationResponse` hoac `ConversationRealtimeEventResponse` moi khi:

- Co message moi trong conversation.
- Current user mark all read.
- Conversation duoc tao.
- Thanh vien duoc them, bi xoa/kick, roi nhom.
- Avatar/metadata conversation duoc cap nhat.
- Biet danh thanh vien duoc cap nhat.
- Truong nhom duoc chuyen.
- Conversation bi an/xoa/giai tan voi user hien tai.

Payload moi cho cac thao tac quan tri conversation:

```json
{
  "event_type": "CONVERSATION_UPSERT",
  "conversation_id": "conversationId",
  "actor_user_id": "userIdThucHien",
  "target_user_id": "userIdBiTacDongNeuCo",
  "conversation": {
    "id": "conversationId"
  },
  "occurred_at": "2026-06-07T16:30:00"
}
```

`event_type`:

- `CONVERSATION_UPSERT`: merge `conversation` vao danh sach chat va refresh info neu dang mo conversation.
- `CONVERSATION_REMOVED`: xoa `conversation_id` khoi danh sach chat cua user nhan event.

### Presence

```text
/topic/presence
```

Payload:

```json
{
  "user_id": "userId",
  "is_online": true
}
```

## Send Destinations

### Send Text Message

```text
SEND /app/chat/{conversationId}
```

Payload:

```json
{
  "content": "Hello",
  "type": "TEXT"
}
```

Behavior:

- Server set `conversationId` tu destination variable.
- IMAGE message bi reject tren WebSocket; hay dung REST multipart.
- FILE message tren WebSocket cung khong co file multipart, nen client nen dung REST multipart cho file.
- Thanh cong se broadcast `MessageResponse` den `/topic/conversation/{conversationId}`.

### Mark All Read

```text
SEND /app/chat/{conversationId}/read
```

Behavior:

- Mark all visible messages as read cho authenticated user.
- Push conversation preview den `/user/queue/conversations`.
- Broadcast `MESSAGES_READ` event den `/topic/conversation/{conversationId}/read`.

### Typing

```text
SEND /app/chat/{conversationId}/typing
```

Payload:

```json
{
  "displayName": "User Name",
  "isTyping": true
}
```

Behavior:

- Server verify user can access conversation.
- Server set `userId` tu principal.
- Broadcast sanitized payload den `/topic/conversation/{conversationId}/typing`.
