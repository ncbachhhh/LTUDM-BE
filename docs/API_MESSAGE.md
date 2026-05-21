# Tai lieu API - Message

Base URL:

```text
http://localhost:8080/api/v1
```

Tat ca endpoint yeu cau:

```http
Authorization: Bearer {accessToken}
```

## Endpoint

### 1. Upload va gui tin nhan anh/file

```http
POST /messages
Content-Type: multipart/form-data
```

Endpoint nay dung de gui message co file binary. Tin nhan `TEXT` van gui qua WebSocket `/app/chat/{conversationId}`.

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

Loai file ho tro:

- Anh: `image/jpeg`, `image/png`, `image/gif`, `image/webp`, toi da 10MB.
- Tai lieu: `application/pdf`, `application/vnd.openxmlformats-officedocument.wordprocessingml.document`, `application/zip`, `application/x-zip-compressed`, `text/plain`.
- Video: `video/mp4`, `video/webm`, `video/quicktime`, `video/x-msvideo`, `video/x-matroska`.
- File tai lieu/video toi da 100MB.

Voi `IMAGE` hoac `FILE`, backend upload file len R2, luu URL vao `messages.content`, va luu metadata vao bang `attachments`.
Sau khi luu thanh cong, backend broadcast message moi len `/topic/conversation/{conversation_id}` va push conversation preview den `/user/queue/conversations`.

Response mau:

```json
{
  "code": 200,
  "data": {
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
    "created_at": "2026-03-12T10:30:00",
    "is_read": false,
    "is_edited": false,
    "edited_at": null,
    "is_recalled": false,
    "recalled_at": null,
    "recalled_by": null
  }
}
```

### 2. Lay tin nhan voi phan trang

```http
GET /messages/conversation/{conversationId}/paged?page=0&size=20
```

Query parameters:

- `page`: so trang, mac dinh `0`.
- `size`: so ban ghi, mac dinh `20`.

Response message `IMAGE`/`FILE` co them field `attachment`; message text co `attachment: null`.

### 3. Danh dau mot tin nhan da doc

```http
PUT /messages/{messageId}/read
```

### 4. Danh dau toan bo tin nhan trong conversation da doc

```http
PUT /messages/conversation/{conversationId}/read-all
```

### 5. Xoa tin nhan phia ca nhan

```http
DELETE /messages/{messageId}
```

Luu y:

- Day la xoa phia ca nhan, duoc luu qua bang `message_deletions`.
- Ban ghi trong `messages` khong bi xoa cung.
- Tin nhan da xoa voi user hien tai se khong con xuat hien trong danh sach cua user do.

### 6. Dem so tin nhan chua doc

```http
GET /messages/conversation/{conversationId}/unread-count
```

### 7. Lay tin nhan moi nhat

```http
GET /messages/conversation/{conversationId}/latest
```

## Ghi chu nghiep vu

- `is_read` trong response duoc suy ra tu bang `message_receipts`, khong nam truc tiep tren bang `messages`.
- Xoa tin nhan phia ca nhan duoc luu o bang `message_deletions`.
- File metadata duoc luu o bang `attachments`; `messages.content` van la URL file de giu tuong thich response cu.
- `IMAGE` va `FILE` khong gui binary qua WebSocket; phai gui bang multipart REST.
- Schema hien co san cac cot recall/edit tren `messages`, nhung service chua implement edit/recall.

## Ma loi

| Code | Message |
|------|---------|
| 400 | Noi dung tin nhan khong duoc de trong |
| 400 | Tin nhan anh can co file anh |
| 400 | Anh tin nhan phai la JPG, PNG, GIF hoac WEBP |
| 400 | Dung luong anh tin nhan khong duoc vuot qua 10MB |
| 400 | Tin nhan file can co file dinh kem |
| 400 | File dinh kem phai la PDF, DOCX, ZIP, TXT hoac video duoc ho tro |
| 400 | Dung luong file dinh kem khong duoc vuot qua 100MB |
| 403 | Ban khong phai thanh vien cua doan chat nay |
| 404 | Khong tim thay tin nhan |
| 404 | Khong tim thay doan chat |
