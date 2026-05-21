# Tai lieu API - Friendship

Base URL:

```text
http://localhost:8080/api/v1
```

Tat ca endpoint trong file nay yeu cau header:

```http
Authorization: Bearer {accessToken}
```

## Khai niem

### Incoming request

`incoming` la loi moi ket ban nguoi khac gui den user dang dang nhap.

Vi du:

- A gui loi moi ket ban cho B.
- Khi B goi API, request do nam trong `incoming`.
- B co quyen `accept` hoac `decline` request nay.

### Outgoing request

`outgoing` la loi moi ket ban user dang dang nhap da gui cho nguoi khac va dang cho xu ly.

Vi du:

- B gui loi moi ket ban cho A.
- Khi B goi API, request do nam trong `outgoing`.
- B khong duoc accept/decline request do, vi nguoi nhan moi co quyen xu ly.

## Trang thai quan he

`friendship_status` trong response profile/user phai khop enum trong database `friendships.status`:

| Gia tri | Y nghia |
|---------|---------|
| `NONE` | Chua co quan he ket ban |
| `PENDING` | Loi moi ket ban dang cho xu ly |
| `ACCEPTED` | Hai user da ket ban thanh cong |
| `DECLINED` | Loi moi ket ban da bi tu choi |
| `BLOCKED` | Quan he dang bi chan, hien chua co API xu ly block |

`friendship_direction` dung de biet chieu cua request khi `friendship_status = PENDING`:

| Gia tri | Y nghia |
|---------|---------|
| `NONE` | Khong co request pending hoac status khong phai `PENDING` |
| `OUTGOING` | User hien tai da gui loi moi |
| `INCOMING` | User hien tai da nhan loi moi |

## Endpoint

### 1. Gui loi moi ket ban

```http
POST /friendships/requests/{userId}
```

`userId` la id cua nguoi muon ket ban.

Quy tac validate:

- Phai dang nhap.
- Khong duoc gui request cho chinh minh.
- User nhan request phai ton tai va dang active.
- Neu da co request/friendship giua 2 user va khong phai `DECLINED`, API tra loi.
- Neu request cu dang `DECLINED`, API se chuyen lai thanh `PENDING` voi requester moi la user hien tai.

Response mau:

```json
{
  "code": 200,
  "data": {
    "id": "uuid-friendship",
    "requester_id": "uuid-current-user",
    "addressee_id": "uuid-target-user",
    "status": "PENDING",
    "created_at": "2026-05-14T09:00:00",
    "updated_at": "2026-05-14T09:00:00",
    "user": {
      "id": "uuid-target-user",
      "email": "target@example.com",
      "username": "target",
      "display_name": "Target User",
      "avatar_url": null,
      "friendship_status": "PENDING",
      "friendship_direction": "OUTGOING"
    },
    "conversation": null
  }
}
```

### 2. Chap nhan loi moi ket ban

```http
POST /friendships/{friendshipId}/accept
```

Quy tac validate:

- Chi nguoi nhan request (`addressee_id`) moi duoc accept.
- Request phai dang `PENDING`.
- Khi accept thanh cong, backend tu tao direct conversation neu chua co.
- Neu direct conversation giua 2 user da ton tai, backend tra ve conversation cu.

### 3. Tu choi loi moi ket ban

```http
POST /friendships/{friendshipId}/decline
```

Quy tac validate:

- Chi nguoi nhan request (`addressee_id`) moi duoc decline.
- Request phai dang `PENDING`.
- Khong tao conversation.

### 4. Thu hoi loi moi da gui

```http
DELETE /friendships/requests/{friendshipId}
```

Quy tac validate:

- Chi nguoi gui request (`requester_id`) moi duoc thu hoi.
- Request phai dang `PENDING`.
- Backend xoa record friendship pending.

Response mau:

```json
{
  "code": 200,
  "data": "Da thu hoi loi moi ket ban."
}
```

### 5. Xoa ban

```http
DELETE /friendships/{friendshipId}
```

Quy tac validate:

- User hien tai phai la `requester_id` hoac `addressee_id`.
- Friendship phai dang `ACCEPTED`.
- Backend xoa record friendship. Direct conversation cu khong bi xoa, nhung REST/WebSocket chat direct se khong cho gui/doc/subscribe vi khong con `ACCEPTED`.

Response mau:

```json
{
  "code": 200,
  "data": "Da xoa ban be."
}
```

### 6. Chan nguoi dung

```http
POST /friendships/blocks/{userId}
```

`userId` la id cua nguoi muon chan.

Quy tac validate:

- Phai dang nhap.
- Khong duoc chan chinh minh.
- User bi chan phai ton tai va dang active.
- Neu da co friendship/request giua 2 user, backend chuyen status sang `BLOCKED`.
- Neu chua co record, backend tao record moi voi `status = BLOCKED`.
- Khi status la `BLOCKED`, 2 user khong duoc chat direct theo rule `ACCEPTED`.

Response mau:

```json
{
  "code": 200,
  "data": {
    "id": "uuid-friendship",
    "requester_id": "uuid-current-user",
    "addressee_id": "uuid-target-user",
    "status": "BLOCKED",
    "created_at": "2026-05-14T09:00:00",
    "updated_at": "2026-05-14T09:10:00",
    "user": {
      "id": "uuid-target-user",
      "email": "target@example.com",
      "username": "target",
      "display_name": "Target User",
      "avatar_url": null,
      "friendship_status": "BLOCKED",
      "friendship_direction": "NONE"
    },
    "conversation": null
  }
}
```

### 7. Lay loi moi gui den minh

```http
GET /friendships/requests/incoming
```

Tra ve cac request `PENDING` ma user hien tai la `addressee_id`.

### 8. Lay loi moi minh da gui

```http
GET /friendships/requests/outgoing
```

Tra ve cac request `PENDING` ma user hien tai la `requester_id`.

### 9. Lay danh sach ban be

```http
GET /friendships
```

Tra ve cac friendship co status `ACCEPTED` lien quan den user hien tai.

### 10. Tim kiem ban be cua toi theo ten

```http
GET /friendships/search?name={name}
```

Mo ta:

- Chi tim trong danh sach ban be da `ACCEPTED` cua user hien tai.
- Tim theo `username` hoac `display_name`.
- Khong tim theo email.
- `name` bat buoc va toi thieu 2 ky tu sau khi trim.
- Gioi han hien tai: 20 ket qua dau tien.

Response mau:

```json
{
  "code": 200,
  "data": [
    {
      "id": "uuid-friend",
      "email": "friend@example.com",
      "username": "friend",
      "display_name": "Friend User",
      "avatar_url": null,
      "friendship_status": "ACCEPTED",
      "friendship_direction": "NONE"
    }
  ]
}
```

## Rule lien quan chat

- Hai user phai co `friendship_status = ACCEPTED` moi duoc tao direct conversation.
- Khi accept friendship thanh cong, backend tu tao direct conversation.
- Neu chua ket ban, API tao conversation se tra `NOT_FRIENDS`.
- REST message va WebSocket direct chat cung kiem tra friendship. Neu friendship khong con `ACCEPTED`, user khong duoc gui/doc/subscribe direct chat do.
- Group chat chi cho phep them member la ban cua nguoi thuc hien thao tac.

## Ma loi

| HTTP | Code | Message |
|------|------|---------|
| 400 | 400 | Khong the ket ban voi chinh minh |
| 400 | 400 | Yeu cau ket ban da ton tai |
| 400 | 400 | Yeu cau ket ban khong con cho xu ly |
| 400 | 400 | Hai nguoi dung chua la ban be |
| 401 | 401 | Chua dang nhap |
| 403 | 403 | Ban khong co quyen xu ly yeu cau ket ban nay |
| 403 | 403 | Khong co quyen truy cap tai nguyen nay |
| 403 | 403 | Hai nguoi dung chua ket ban |
| 404 | 404 | Khong tim thay yeu cau ket ban |
| 404 | 404 | Khong tim thay nguoi dung |
