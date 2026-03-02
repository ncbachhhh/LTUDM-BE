# WebSocket API - Realtime Chat

**WebSocket URL:** `ws://localhost:8080/ws`

---

## 1. Kết nối WebSocket

### STOMP Connect

```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect(
    { Authorization: 'Bearer ' + accessToken },
    onConnected,
    onError
);
```

**Header bắt buộc:**
| Key | Value |
|-----|-------|
| Authorization | Bearer {accessToken} |

---

## 2. Subscribe (Nhận tin nhắn)

### 2.1 Tin nhắn trong conversation

```
/topic/conversation/{conversationId}
```

```javascript
stompClient.subscribe('/topic/conversation/123', function(message) {
    const msg = JSON.parse(message.body);
    console.log(msg);
});
```

### 2.2 Tin nhắn riêng cho user

```
/user/queue/messages
```

```javascript
stompClient.subscribe('/user/queue/messages', function(message) {
    const msg = JSON.parse(message.body);
    console.log(msg);
});
```

---

## 3. Send (Gửi tin nhắn)

### 3.1 Gửi tin nhắn

```
/app/chat.send
```

```javascript
stompClient.send('/app/chat.send', {}, JSON.stringify({
    conversationId: '123',
    content: 'Hello!',
    type: 'TEXT'
}));
```

### 3.2 Đang gõ (typing)

```
/app/chat.typing
```

```javascript
stompClient.send('/app/chat.typing', {}, JSON.stringify({
    conversationId: '123',
    isTyping: true
}));
```

---

## 4. Message Format

### Tin nhắn gửi đi

```json
{
    "conversationId": "uuid",
    "content": "Hello!",
    "type": "TEXT"
}
```

| Field | Type | Description |
|-------|------|-------------|
| conversationId | string | ID cuộc trò chuyện |
| content | string | Nội dung tin nhắn |
| type | string | TEXT, IMAGE, VIDEO, FILE |

### Tin nhắn nhận về

```json
{
    "id": "uuid",
    "conversationId": "uuid",
    "senderId": "uuid",
    "senderName": "John Doe",
    "content": "Hello!",
    "type": "TEXT",
    "createdAt": "2026-03-02T10:30:00"
}
```

---

## 5. Ví dụ đầy đủ (JavaScript)

```javascript
// Kết nối
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect(
    { Authorization: 'Bearer ' + accessToken },
    function(frame) {
        console.log('Connected');

        // Subscribe conversation
        stompClient.subscribe('/topic/conversation/123', function(message) {
            const msg = JSON.parse(message.body);
            displayMessage(msg);
        });

        // Subscribe tin nhắn riêng
        stompClient.subscribe('/user/queue/messages', function(message) {
            const msg = JSON.parse(message.body);
            displayMessage(msg);
        });
    },
    function(error) {
        console.log('Error: ' + error);
    }
);

// Gửi tin nhắn
function sendMessage(conversationId, content) {
    stompClient.send('/app/chat.send', {}, JSON.stringify({
        conversationId: conversationId,
        content: content,
        type: 'TEXT'
    }));
}

// Disconnect
function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
}
```

---

## 6. Error Codes

| Code | Message |
|------|---------|
| 401 | Unauthenticated - Token không hợp lệ |
| 401 | Token expired - Token hết hạn |

---

## 7. Ghi chú

- WebSocket sử dụng **STOMP protocol**
- Token được xác thực khi **CONNECT**
- Nếu token hết hạn, cần reconnect với token mới
