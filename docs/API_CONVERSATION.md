# API Conversation

Base URL: `/api/v1/conversations`

## Endpoints

### 1. Tạo đoạn chat cá nhân hoặc nhóm
```http
POST /conversations
```

Request tạo chat cá nhân:
```json
{
  "type": "DIRECT",
  "member_ids": ["uuid-user-b"]
}
```

Request tạo chat nhóm:
```json
{
  "type": "GROUP",
  "title": "Nhóm dự án",
  "avatar_url": "https://example.com/group.png",
  "member_ids": ["uuid-user-b", "uuid-user-c"]
}
```

Quy tắc:
- `DIRECT` chỉ nhận đúng 1 `member_id` khác user hiện tại.
- Nếu chat cá nhân giữa 2 user đã tồn tại, API trả lại đoạn chat cũ thay vì tạo mới.
- `GROUP` bắt buộc có `title`.
- User tạo nhóm luôn được thêm vào nhóm với role `OWNER`.

Response mẫu:
```json
{
  "code": 200,
  "data": {
    "id": "uuid-conversation",
    "type": "GROUP",
    "title": "Nhóm dự án",
    "created_by": "uuid-owner",
    "created_at": "2026-04-14T10:00:00",
    "avatar_url": "https://example.com/group.png",
    "members": [
      {
        "user_id": "uuid-owner",
        "username": "owner",
        "display_name": "Chủ nhóm",
        "avatar_url": null,
        "role": "OWNER",
        "joined_at": "2026-04-14T10:00:00"
      }
    ]
  }
}
```

### 2. Thêm thành viên vào nhóm
```http
POST /conversations/{conversationId}/members
```

Request:
```json
{
  "member_ids": ["uuid-user-d", "uuid-user-e"]
}
```

Quy tắc:
- Chỉ áp dụng cho `GROUP`.
- Chỉ `OWNER` hoặc `ADMIN` mới thêm được thành viên.
- Nếu toàn bộ user trong request đã có sẵn trong nhóm, API trả lỗi.

### 3. Xóa nhóm chat
```http
DELETE /conversations/{conversationId}
```

Quy tắc:
- Chỉ áp dụng cho `GROUP`.
- Chỉ `OWNER` hoặc `ADMIN` mới xóa được nhóm.

Response:
```json
{
  "code": 200,
  "data": "Group conversation deleted"
}
```

## Error codes

| Code | Message |
|------|---------|
| 400 | Conversation type must be DIRECT or GROUP |
| 400 | Direct conversation must contain exactly one other user |
| 400 | Group conversation must contain at least two members |
| 400 | Conversation members are invalid |
| 400 | Group conversation title is required |
| 400 | All users are already members of this conversation |
| 400 | This operation is only allowed for group conversations |
| 403 | You are not a member of this conversation |
| 403 | You do not have permission to manage this group |
| 404 | User not found |
| 404 | Conversation not found |
