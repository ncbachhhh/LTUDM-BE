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

### Conversation Read Event

```text
/topic/conversation/{conversationId}/read
```

Nhan text event khi user mark all read qua STOMP.

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

Nhan `ConversationResponse` moi khi:

- Co message moi trong conversation.
- Current user mark all read.

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
- Broadcast text event den `/topic/conversation/{conversationId}/read`.

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
