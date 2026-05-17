# Tài Liệu API - Conversation

Base URL:

```text
http://localhost:8080/api/v1
```

## Endpoint

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
  "data": "Đã xóa nhóm chat."
}
```

### 4. Lấy danh sách conversation của user hiện tại

```http
GET /conversations/me
```

Mô tả:

- Lấy danh sách tất cả conversation mà user hiện tại tham gia.
- User id được lấy tự động từ token JWT.
- Không cần truyền bất kỳ parameter nào.

Quy tắc:

- Endpoint này luôn lấy conversation của user đang đăng nhập.
- Danh sách trả về được sắp xếp theo `created_at` giảm dần, conversation mới nhất ở đầu.
- Nếu user không có conversation nào, API trả về mảng rỗng `[]`.

Response mẫu:

```json
{
  "code": 200,
  "data": [
    {
      "id": "uuid-conv-1",
      "type": "DIRECT",
      "title": null,
      "created_by": "uuid-user-a",
      "created_at": "2026-04-10T15:30:00",
      "avatar_url": null,
      "members": [
        {
          "user_id": "uuid-user-a",
          "username": "user_a",
          "display_name": "User A",
          "avatar_url": "https://example.com/avatar-a.jpg",
          "role": "OWNER",
          "joined_at": "2026-04-10T15:30:00"
        },
        {
          "user_id": "uuid-user-b",
          "username": "user_b",
          "display_name": "User B",
          "avatar_url": "https://example.com/avatar-b.jpg",
          "role": "MEMBER",
          "joined_at": "2026-04-10T15:30:00"
        }
      ]
    }
  ]
}
```

Quy tắc response:

- Danh sách trả về được sắp xếp theo `created_at` giảm dần.
- Nếu user không có conversation nào, API trả về mảng rỗng `[]`.
- Mỗi conversation bao gồm danh sách members kèm role và thời gian tham gia.

## Rule ket ban khi chat

- Tao `DIRECT` conversation chi thanh cong khi 2 user da ket ban (`friendship.status = ACCEPTED`).
- Khi accept loi moi ket ban thanh cong, backend tu tao direct conversation neu chua co.
- Tao `GROUP` yeu cau tat ca `member_ids` la ban cua user tao nhom.
- Them member vao group yeu cau member moi la ban cua user thuc hien thao tac.
- REST message va WebSocket direct chat cung kiem tra friendship. Neu 2 user khong con la ban, backend tra loi `NOT_FRIENDS`.

## Mã lỗi

| Code | Message |
|------|---------|
| 400 | Loại đoạn chat phải là DIRECT hoặc GROUP |
| 400 | Chat cá nhân phải có đúng một người dùng khác |
| 400 | Chat nhóm phải có ít nhất hai thành viên |
| 400 | Danh sách thành viên đoạn chat không hợp lệ |
| 400 | Tên nhóm là bắt buộc |
| 400 | Tất cả người dùng đã là thành viên của đoạn chat này |
| 400 | Thao tác này chỉ áp dụng cho chat nhóm |
| 401 | Chưa đăng nhập |
| 403 | Không có quyền truy cập tài nguyên này |
| 403 | Bạn không phải thành viên của đoạn chat này |
| 403 | Bạn không có quyền quản lý nhóm này |
| 404 | Không tìm thấy người dùng |
| 404 | Không tìm thấy đoạn chat |
