# FE Chat Integration Notes

Last updated: 2026-05-21

Tai lieu nay ghi lai cac API/backend event de FE thay phan du lieu dang fix cung trong man hinh chat.

Base URL:

```text
http://localhost:8080/api/v1
```

WebSocket endpoint:

```text
ws://localhost:8080/api/v1/ws
```

Header HTTP va STOMP connect deu dung:

```text
Authorization: Bearer {accessToken}
```

## 1. Danh sach doan chat

### API

```http
GET /conversations/me
```

Moi item conversation co:

```json
{
  "id": "uuid-conversation",
  "type": "DIRECT",
  "title": null,
  "created_by": "uuid-user-a",
  "created_at": "2026-05-20T09:00:00",
  "avatar_url": null,
  "latest_message": {
    "id": "uuid-message",
    "conversation_id": "uuid-conversation",
    "sender_id": "uuid-user-b",
    "type": "FILE",
    "content": "https://public-r2-domain/messages/conversation/sender/files/random.pdf",
    "attachment": {
      "id": "uuid-attachment",
      "file_url": "https://public-r2-domain/messages/conversation/sender/files/random.pdf",
      "file_name": "document.pdf",
      "mime_type": "application/pdf",
      "file_size": 123456
    },
    "created_at": "2026-05-20T09:10:00",
    "is_read": false,
    "is_edited": false,
    "edited_at": null,
    "is_recalled": false,
    "recalled_at": null,
    "recalled_by": null
  },
  "unread_count": 3,
  "members": []
}
```

FE nen hien thi:

- Preview text theo `latest_message.type`.
- `TEXT`: dung `latest_message.content`.
- `IMAGE`: dung text ngan nhu `Da gui mot anh`.
- `FILE`: dung `latest_message.attachment.file_name`, fallback `Da gui mot file`.
- `unread_count > 0` de hien badge/chu dam.
- Neu `latest_message` la `null`, fallback text nhu `Bam de mo doan chat`.
- Danh sach da duoc backend sort theo `latest_message.created_at`, fallback `created_at`.

## 2. Realtime cap nhat danh sach chat

### Subscribe

```text
/user/queue/conversations
```

Payload nhan duoc la mot `ConversationResponse` giong item trong `GET /conversations/me`.

Khi nhan event, FE nen:

1. Tim conversation trong state theo `id`.
2. Replace item cu bang payload moi.
3. Neu chua co item, insert vao dau danh sach.
4. Sort lai theo `latest_message.created_at || created_at`.

Event nay duoc backend gui khi:

- Co tin nhan text moi qua WebSocket `/app/chat/{conversationId}`.
- Co tin nhan anh/file qua HTTP `POST /messages`.
- User danh dau da doc qua WebSocket `/app/chat/{conversationId}/read`.
- User danh dau da doc qua HTTP `PUT /messages/conversation/{conversationId}/read-all`.

## 3. Gui tin nhan text

Text gui qua STOMP:

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

Backend broadcast message moi ve:

```text
/topic/conversation/{conversationId}
```

## 4. Gui anh va file

Anh/file binary khong gui qua STOMP. FE goi REST multipart:

```http
POST /messages
Content-Type: multipart/form-data
Authorization: Bearer {accessToken}
```

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

Loai file backend ho tro:

- Anh: `image/jpeg`, `image/png`, `image/gif`, `image/webp`, toi da 10MB.
- Tai lieu: `application/pdf`, `application/vnd.openxmlformats-officedocument.wordprocessingml.document`, `application/zip`, `application/x-zip-compressed`, `text/plain`.
- Video: `video/mp4`, `video/webm`, `video/quicktime`, `video/x-msvideo`, `video/x-matroska`.
- File tai lieu/video toi da 100MB.

Response message `IMAGE`/`FILE`:

```json
{
  "id": "uuid-message",
  "conversation_id": "uuid-conversation",
  "sender_id": "uuid-sender",
  "type": "FILE",
  "content": "https://public-r2-domain/messages/uuid-conversation/uuid-sender/files/random.pdf",
  "attachment": {
    "id": "uuid-attachment",
    "file_url": "https://public-r2-domain/messages/uuid-conversation/uuid-sender/files/random.pdf",
    "file_name": "document.pdf",
    "mime_type": "application/pdf",
    "file_size": 123456
  },
  "created_at": "2026-05-21T10:30:00",
  "is_read": false,
  "is_edited": false,
  "edited_at": null,
  "is_recalled": false,
  "recalled_at": null,
  "recalled_by": null
}
```

Sau khi upload thanh cong, backend:

- Luu URL vao `messages.content`.
- Luu metadata vao `attachments`.
- Broadcast message moi ve `/topic/conversation/{conversationId}`.
- Push conversation preview ve `/user/queue/conversations`.

FE nen render:

- `IMAGE`: render image bang `attachment.file_url || content`.
- `FILE`: render file card voi `attachment.file_name`, `attachment.file_size`, `attachment.mime_type`, link tai/mo bang `attachment.file_url || content`.
- Neu `attachment` null nhung `content` la URL, van fallback mo URL tu `content`.

## 5. Lay tin nhan theo trang

```http
GET /messages/conversation/{conversationId}/paged?page=0&size=20
```

Response message `IMAGE`/`FILE` co `attachment`; message text co `attachment: null`.

## 6. Thong tin panel ben phai

### API

```http
GET /conversations/{conversationId}/info
```

Response mau:

```json
{
  "code": 200,
  "data": {
    "id": "uuid-conversation",
    "type": "GROUP",
    "title": "Nhom du an",
    "display_name": "Nhom du an",
    "avatar_url": "https://example.com/group.png",
    "status": "Nhom chat",
    "created_by": "uuid-owner",
    "created_at": "2026-05-20T09:00:00",
    "member_count": 4,
    "members": [
      {
        "user_id": "uuid-user",
        "username": "user01",
        "display_name": "Nguyen Van A",
        "nickname": "A Dev",
        "avatar_url": null,
        "role": "MEMBER",
        "joined_at": "2026-05-20T09:00:00"
      }
    ],
    "stats": [
      { "id": "members", "label": "Thanh vien", "value": "4", "subValue": null },
      { "id": "links", "label": "Link", "value": "2", "subValue": null },
      { "id": "files", "label": "File", "value": "1", "subValue": null },
      { "id": "images", "label": "Hinh anh", "value": "3", "subValue": null }
    ],
    "settings": [
      "Chinh sua biet danh",
      "Thay doi bieu tuong cam xuc"
    ]
  }
}
```

FE nen dung:

- Header panel: `display_name`, `avatar_url`, `status`.
- Thong ke: render truc tiep tu `stats`.
- Cai dat: render truc tiep tu `settings`.
- Thanh vien/modal biet danh: dung `members`.

Voi chat ca nhan, `display_name` uu tien `nickname`, fallback `display_name`, roi `username`.

## 7. Sua biet danh thanh vien

### API

```http
PATCH /conversations/{conversationId}/members/{memberId}/nickname
```

Request:

```json
{
  "nickname": "Biet danh moi"
}
```

Quy tac:

- Nguoi goi phai la thanh vien cua conversation.
- `memberId` phai la thanh vien cua conversation.
- `nickname` toi da 100 ky tu.
- Gui `""`, `"   "` hoac `null` de xoa biet danh.
- Dung duoc cho ca chat ca nhan va chat nhom.

Response la `ConversationResponse` da cap nhat, trong do member co field:

```json
{
  "user_id": "uuid-user",
  "display_name": "Ten that",
  "nickname": "Biet danh moi"
}
```

## 8. Goi y mapping FE

Ten hien thi conversation:

```javascript
const getMemberName = (member) =>
  member?.nickname || member?.display_name || member?.displayName || member?.username || "Nguoi dung";
```

Preview tin nhan:

```javascript
const getPreviewText = (conversation) => {
  const message = conversation.latest_message || conversation.latestMessage;
  if (!message) return "Bam de mo doan chat";

  if (message.type === "IMAGE") return "Da gui mot anh";
  if (message.type === "FILE") {
    return message.attachment?.file_name || message.attachment?.fileName || "Da gui mot file";
  }

  return message.content || "Bam de mo doan chat";
};
```

Format file size:

```javascript
const formatFileSize = (bytes = 0) => {
  if (!bytes) return "0 B";
  const units = ["B", "KB", "MB", "GB"];
  const index = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1);
  return `${(bytes / Math.pow(1024, index)).toFixed(index === 0 ? 0 : 1)} ${units[index]}`;
};
```

Badge chua doc:

```javascript
const unreadCount = conversation.unread_count || conversation.unreadCount || 0;
```

Realtime subscribe:

```javascript
client.subscribe("/user/queue/conversations", (message) => {
  const updatedConversation = JSON.parse(message.body);
  // replace/insert by updatedConversation.id, then sort by latest_message.created_at || created_at
});
```

Gui file bang `FormData`:

```javascript
const sendFileMessage = async ({ conversationId, file, accessToken }) => {
  const formData = new FormData();
  formData.append(
    "message",
    new Blob(
      [JSON.stringify({ conversation_id: conversationId, type: "FILE" })],
      { type: "application/json" }
    )
  );
  formData.append("file", file);

  const response = await fetch("http://localhost:8080/api/v1/messages", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
    body: formData,
  });

  return response.json();
};
```
