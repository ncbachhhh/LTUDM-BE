# FE Chat Integration Notes

Tài liệu này ghi lại các API/backend event mới để FE thay phần dữ liệu đang fix cứng trong màn hình chat.

Base URL:

```text
http://localhost:8080/api/v1
```

WebSocket endpoint:

```text
ws://localhost:8080/api/v1/ws
```

Header HTTP và STOMP connect đều dùng:

```text
Authorization: Bearer {accessToken}
```

## 1. Danh sách đoạn chat

### API

```http
GET /conversations/me
```

Mỗi item conversation hiện có thêm:

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
    "type": "TEXT",
    "content": "Tin nhắn mới nhất",
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

FE nên hiển thị:

- `latest_message.content` cho dòng preview tin nhắn cuối.
- `unread_count > 0` để hiện badge/chữ đậm.
- Nếu `latest_message` là `null`, fallback text như “Bấm để mở đoạn chat”.
- Danh sách đã được backend sort theo `latest_message.created_at`, fallback `created_at`.

## 2. Realtime cập nhật danh sách chat

### Subscribe

```text
/user/queue/conversations
```

Payload nhận được là một `ConversationResponse` giống item trong `GET /conversations/me`.

Khi nhận event, FE nên:

1. Tìm conversation trong state theo `id`.
2. Replace item cũ bằng payload mới.
3. Nếu chưa có item, insert vào đầu danh sách.
4. Sort lại theo `latest_message.created_at || created_at`.

Event này được backend gửi khi:

- Có tin nhắn text mới qua WebSocket `/app/chat/{conversationId}`.
- Có tin nhắn ảnh/file qua HTTP `POST /messages`.
- User đánh dấu đã đọc qua WebSocket `/app/chat/{conversationId}/read`.
- User đánh dấu đã đọc qua HTTP `PUT /messages/conversation/{conversationId}/read-all`.

## 3. Thông tin panel bên phải

### API

```http
GET /conversations/{conversationId}/info
```

Response mẫu:

```json
{
  "code": 200,
  "data": {
    "id": "uuid-conversation",
    "type": "GROUP",
    "title": "Nhóm dự án",
    "display_name": "Nhóm dự án",
    "avatar_url": "https://example.com/group.png",
    "status": "Nhóm chat",
    "created_by": "uuid-owner",
    "created_at": "2026-05-20T09:00:00",
    "member_count": 4,
    "members": [
      {
        "user_id": "uuid-user",
        "username": "user01",
        "display_name": "Nguyễn Văn A",
        "nickname": "A Dev",
        "avatar_url": null,
        "role": "MEMBER",
        "joined_at": "2026-05-20T09:00:00"
      }
    ],
    "stats": [
      { "id": "members", "label": "Thành viên", "value": "4", "subValue": null },
      { "id": "links", "label": "Link", "value": "2", "subValue": null },
      { "id": "files", "label": "File", "value": "1", "subValue": null },
      { "id": "images", "label": "Hình ảnh", "value": "3", "subValue": null }
    ],
    "settings": [
      "Chỉnh sửa biệt danh",
      "Thay đổi biểu tượng cảm xúc"
    ]
  }
}
```

FE nên dùng:

- Header panel: `display_name`, `avatar_url`, `status`.
- Thống kê: render trực tiếp từ `stats`.
- Cài đặt: render trực tiếp từ `settings`.
- Thành viên/modal biệt danh: dùng `members`.

Với chat cá nhân, `display_name` ưu tiên `nickname`, fallback `display_name`, rồi `username`.

## 4. Sửa biệt danh thành viên

### API

```http
PATCH /conversations/{conversationId}/members/{memberId}/nickname
```

Request:

```json
{
  "nickname": "Biệt danh mới"
}
```

Quy tắc:

- Người gọi phải là thành viên của conversation.
- `memberId` phải là thành viên của conversation.
- `nickname` tối đa 100 ký tự.
- Gửi `""`, `"   "` hoặc `null` để xóa biệt danh.
- Dùng được cho cả chat cá nhân và chat nhóm.

Response là `ConversationResponse` đã cập nhật, trong đó member có field:

```json
{
  "user_id": "uuid-user",
  "display_name": "Tên thật",
  "nickname": "Biệt danh mới"
}
```

## 5. Gợi ý mapping FE

Tên hiển thị conversation:

```javascript
const getMemberName = (member) =>
  member?.nickname || member?.display_name || member?.displayName || member?.username || "Người dùng";
```

Preview tin nhắn:

```javascript
const getPreviewText = (conversation) =>
  conversation.latest_message?.content || "Bấm để mở đoạn chat";
```

Badge chưa đọc:

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
