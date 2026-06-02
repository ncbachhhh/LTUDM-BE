# API Reference

## Auth APIs

### Register

```http
POST /auth/register
Content-Type: application/json
```

Request:

```json
{
  "email": "user@example.com",
  "username": "username",
  "password": "password123",
  "display_name": "User Name",
  "role": "USER"
}
```

Behavior:

- Tao user moi.
- Email va username phai chua ton tai.
- Password duoc hash bang BCrypt.
- Neu `role` null, service gan `USER`.
- User moi duoc set `active = true`.

Response `data`: `UserResponse`.

### Login

```http
POST /auth/login
Content-Type: application/json
```

Request:

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

Behavior:

- Tim user theo email.
- Kiem tra password hash.
- Chan login neu user inactive.
- Sinh access token va refresh token, cung la HS256 JWT.
- Claim chinh: `sub = userId`, `scope = ROLE_<role>`, `iss = LTUDM`, `jti`, `exp`.

Response:

```json
{
  "code": 200,
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "isAuthenticated": true
  }
}
```

### Refresh Token

```http
POST /auth/refresh
Content-Type: application/json
```

Request:

```json
{
  "refreshToken": "<refresh_token>"
}
```

Behavior:

- Verify refresh token signature, expiration, revoked state.
- Luu `jti` refresh token cu vao `invalidated_tokens`.
- Tim user trong token subject.
- Neu user active, cap access token va refresh token moi.

### Logout

```http
POST /auth/logout
Content-Type: application/json
```

Request:

```json
{
  "token": "<token>"
}
```

Behavior:

- Verify token.
- Neu token hop le, luu `jti` vao `invalidated_tokens`.
- Neu token da invalid/expired thi service bo qua va van tra success.

### Forgot Password

```http
POST /auth/forgot-password
Content-Type: application/json
```

Request:

```json
{
  "email": "user@example.com"
}
```

Behavior:

- Normalize email ve lowercase.
- Neu email ton tai va khong trong cooldown 60 giay, tao OTP 6 so.
- OTP duoc hash bang password encoder va luu Redis trong 5 phut.
- Xoa attempts cu, set cooldown, gui email OTP.
- API luon tra success message de khong leak email ton tai hay khong.

### Verify Reset OTP

```http
POST /auth/verify-reset-otp
Content-Type: application/json
```

Request:

```json
{
  "email": "user@example.com",
  "otp": "123456"
}
```

Behavior:

- Kiem tra email ton tai.
- Lay OTP hash tu Redis.
- Gioi han 5 lan sai. Vuot gioi han se xoa OTP hien tai.
- Neu OTP dung, tao reset token random, luu Redis 10 phut, xoa OTP va attempts.

Response `data`:

```json
{
  "resetToken": "<reset_token>"
}
```

### Reset Password

```http
POST /auth/reset-password
Content-Type: application/json
```

Request:

```json
{
  "resetToken": "<reset_token>",
  "newPassword": "newPassword123"
}
```

Behavior:

- Hash reset token bang SHA-256 de tim Redis key.
- Tim email tu Redis, tim user theo email.
- Hash password moi va save user.
- Xoa reset token, OTP, attempts, cooldown.

## User APIs

All endpoints require `Authorization: Bearer <access_token>`.

### Get My Info

```http
GET /users/me
```

Response `data`: `UserResponse` cua user trong token.

### Update User

```http
PATCH /users/{userId}
Content-Type: application/json
```

Request:

```json
{
  "display_name": "New Display Name",
  "avatar_url": "https://example.com/avatar.png"
}
```

Behavior:

- Chi owner moi duoc update, duoc enforce bang `@userSecurity.isOwner`.
- Field null duoc ignore boi MapStruct.
- Khong cho update id, email, username, password, role, active.

### Update My Avatar

```http
PATCH /users/me/avatar
Content-Type: multipart/form-data
```

Form:

```text
file=<image file>
```

Behavior:

- File bat buoc.
- Content type: JPG, PNG, GIF, WEBP.
- Size toi da 5MB.
- File upload len R2 folder `avatars/{userId}`.
- `avatarUrl` cua user duoc update bang URL file.

### Update My Settings

```http
PATCH /users/settings
Content-Type: application/json
```

```http
PUT /users/settings
Content-Type: application/json
```

Request:

```json
{
  "showBirthday": "full",
  "onlineStatus": true,
  "showEmail": true,
  "mentionSuggestions": true,
  "readReceipts": true,
  "notificationEnabled": true,
  "soundEnabled": true,
  "theme_mode": "dark",
  "chat_color": "linear-gradient(135deg, rgb(255, 0, 204) 0%, rgb(51, 51, 153) 100%)"
}
```

Behavior:

- Current user duoc lay tu access token.
- Tat ca fields deu optional; field null se khong update.
- `theme_mode` chi nhan `light` hoac `dark`.
- `chat_color` la CSS background value cho bubble/chat color, vi du `#0A84FF`, `rgb(0, 51, 255)`, hoac `linear-gradient(...)`.
- `chat_color` toi da 500 ky tu va backend chan `url(...)`, `;`, `{}`, `<`, `>` de tranh CSS injection co ban.
- `theme_mode` va `chat_color` luu tren bang `users`, ap dung cho toan bo UI/chat cua current user.
- Thiet lap nay chi anh huong cach current user nhin thay giao dien, khong thay doi conversation cho user khac.

Response `data`: `UserResponse`, bao gom:

```json
{
  "theme_mode": "dark",
  "chat_color": "linear-gradient(135deg, rgb(255, 0, 204) 0%, rgb(51, 51, 153) 100%)"
}
```

### Change Password

```http
POST /users/me/change-password
Content-Type: application/json
```

Request:

```json
{
  "old_password": "oldPassword123",
  "new_password": "newPassword123",
  "confirm_password": "newPassword123"
}
```

Behavior:

- Kiem tra old password.
- New password phai match confirm password.
- New password phai khac password hien tai.
- Luu BCrypt hash moi.

### Search User By Email

```http
GET /users/search-by-email?email=user@example.com
```

Behavior:

- Search exact email ignore case.
- Khong tra ve current user.
- Khong tra ve inactive user.
- Response gom friendship state va online state.

### Search Users For Friend Request

```http
GET /users/search?keyword=abc
```

Behavior:

- Keyword toi thieu 2 ky tu.
- Search email, username, display name cua active users, exclude current user.
- Gioi han 20 ket qua.
- Response gom friendship state va online state.

### Get User Profile

```http
GET /users/{userId}/profile
```

Behavior:

- Khong cho lay profile cua chinh minh qua endpoint nay.
- User phai ton tai va active.
- Response gom friendship state va online state.

## Friendship APIs

All endpoints require auth.

### Send Request

```http
POST /friendships/requests/{userId}
```

Behavior:

- Khong cho ket ban voi chinh minh.
- Target user phai ton tai va active.
- Neu relationship da ton tai va status `DECLINED`, request duoc reuse thanh `PENDING`.
- Neu relationship da ton tai status khac, tra `FRIENDSHIP_ALREADY_EXISTS`.

### Accept Request

```http
POST /friendships/{friendshipId}/accept
```

Behavior:

- Current user phai la addressee.
- Friendship phai `PENDING`.
- Set status `ACCEPTED`.
- Tim hoac tao direct conversation giua requester va addressee.
- Response gom friendship va conversation.

### Decline Request

```http
POST /friendships/{friendshipId}/decline
```

Behavior:

- Current user phai la addressee.
- Friendship phai `PENDING`.
- Set status `DECLINED`.

### Withdraw Request

```http
DELETE /friendships/requests/{friendshipId}
```

Behavior:

- Current user phai la requester.
- Friendship phai `PENDING`.
- Xoa friendship record.

### Delete Friend

```http
DELETE /friendships/{friendshipId}
```

Behavior:

- Current user phai la requester hoac addressee.
- Status phai `ACCEPTED` hoac `BLOCKED`.
- Neu status `BLOCKED`, chi requester cua legacy blocked friendship duoc xoa.
- Xoa friendship record.

### Block User

```http
POST /friendships/blocks/{userId}
```

Behavior:

- Target user phai hop le.
- Tao record trong `blocks` neu chua co.
- Neu co legacy friendship status `BLOCKED` do current user tao, set lai `ACCEPTED`.
- Response status luon `BLOCKED` tu goc nhin current user.

### Unblock User

```http
DELETE /friendships/blocks/{userId}
```

Behavior:

- Xoa record block trong `blocks`.
- Ho tro legacy blocked friendship: neu co status `BLOCKED` do current user tao, set lai `ACCEPTED`.
- Neu khong co block nao, tra `USER_NOT_BLOCKED`.

### Get Blocked Users

```http
GET /friendships/blocks
```

Behavior:

- Tra danh sach user current user dang block tu bang `blocks`.
- Gop them legacy blocked friendship neu co.

### Incoming Requests

```http
GET /friendships/requests/incoming
```

Tra request `PENDING` co `addresseeId = currentUserId`.

### Outgoing Requests

```http
GET /friendships/requests/outgoing
```

Tra request `PENDING` co `requesterId = currentUserId`.

### Get Friends

```http
GET /friendships
```

Tra friendship status `ACCEPTED` lien quan current user.

### Search My Friends

```http
GET /friendships/search?name=abc
```

Behavior:

- Name toi thieu 2 ky tu.
- Search accepted friends theo username hoac display name.
- Gioi han 20 ket qua.

## Conversation APIs

All endpoints require auth.

### Get My Conversations

```http
GET /conversations/me
```

Behavior:

- Lay conversation ma current user la member.
- Moi item gom members, online state, latest visible message, unread count.
- Direct conversation gom friendship state va block flags.
- Sort theo latest message time, fallback created time.

### Get Conversation Info

```http
GET /conversations/{conversationId}/info
```

Behavior:

- Current user phai la member.
- Response gom display name, avatar, members, stats.
- Stats gom member count, link count, file count, image count.

### Create Conversation

```http
POST /conversations
Content-Type: application/json
```

Direct request:

```json
{
  "type": "DIRECT",
  "member_ids": ["targetUserId"]
}
```

Behavior:

- Exactly one target user.
- Target user phai ton tai.
- Hai user phai la accepted friends va khong bi block.
- Neu direct conversation da ton tai, tra conversation do.
- Neu chua co, tao conversation moi va 2 members.

Group request:

```json
{
  "type": "GROUP",
  "title": "Project Group",
  "avatar_url": "https://example.com/group.png",
  "member_ids": ["userA", "userB"]
}
```

Behavior:

- Title bat buoc.
- Current user duoc them vao members va role `OWNER`.
- Group phai co it nhat 2 members sau khi them current user.
- Current user phai la accepted friend voi tat ca member duoc add.

### Add Group Members

```http
POST /conversations/{conversationId}/members
Content-Type: application/json
```

Request:

```json
{
  "member_ids": ["userA", "userB"]
}
```

Behavior:

- Conversation phai la `GROUP`.
- Current user phai la owner.
- Bo qua member da ton tai; neu tat ca da ton tai, tra loi.
- Current user phai la accepted friend voi member moi.
- Save new members role `MEMBER`.

### Update Member Nickname

```http
PATCH /conversations/{conversationId}/members/{memberId}/nickname
Content-Type: application/json
```

Request:

```json
{
  "nickname": "Nick"
}
```

Behavior:

- Current user phai la member cua conversation.
- Target member phai ton tai trong conversation.
- Nickname blank duoc save thanh null.
- Max 100 ky tu.

### Delete Group Conversation

```http
DELETE /conversations/{conversationId}
```

Behavior:

- Conversation phai la `GROUP`.
- Current user phai la owner.
- Xoa message receipts, message deletions, messages.
- Xoa conversation members.
- Xoa conversation.

## Message APIs

All endpoints require auth.

### Send Message With Multipart

```http
POST /messages
Content-Type: multipart/form-data
```

Parts:

```text
message=<JSON MessageRequest>
file=<optional MultipartFile>
```

MessageRequest:

```json
{
  "conversation_id": "conversationId",
  "content": "Hello",
  "type": "TEXT"
}
```

Types:

- `TEXT`: `content` bat buoc sau khi trim, `file` khong duoc gui.
- `IMAGE`: `file` bat buoc, content se la URL R2 sau upload.
- `FILE`: `file` bat buoc, content se la URL R2 sau upload.

Behavior:

- Sender phai authenticated.
- Sender phai la member cua conversation.
- Neu direct conversation, hai user phai la accepted friends va khong bi block.
- Message duoc save.
- Neu IMAGE/FILE, attachment record duoc tao voi file metadata.
- Controller broadcast message den `/topic/conversation/{conversationId}`.
- Controller push conversation preview den tung member qua `/user/queue/conversations`.

### Get Messages Paged

```http
GET /messages/conversation/{conversationId}/paged?page=0&size=20
```

Behavior:

- Current user phai la member.
- Chi tra messages chua bi current user soft-delete.
- Sort theo `createdAt DESC` trong repository paging.
- Moi message gom attachment neu co va `is_read` theo current user.

### Mark Message As Read

```http
PUT /messages/{messageId}/read
```

Behavior:

- Message phai ton tai.
- Current user phai access duoc conversation.
- Neu current user la sender thi bo qua.
- Neu chua co receipt, tao `MessageReceipt`.

### Mark All As Read

```http
PUT /messages/conversation/{conversationId}/read-all
```

Behavior:

- Current user phai access duoc conversation.
- Tao receipt cho tat ca visible messages khong phai do current user gui va chua co receipt.
- Push conversation preview moi cho current user.

### Soft Delete Message

```http
DELETE /messages/{messageId}
```

Behavior:

- Message phai ton tai.
- Current user phai access duoc conversation.
- Tao `MessageDeletion(messageId, userId)` neu chua co.
- Khong xoa message khoi DB.

### Count Unread

```http
GET /messages/conversation/{conversationId}/unread-count
```

Tra so message visible, khong phai current user gui, va chua co receipt.

### Get Latest Message

```http
GET /messages/conversation/{conversationId}/latest
```

Tra message visible moi nhat cua current user trong conversation, hoac `null`.

### Pin Message

```http
PUT /messages/{messageId}/pin
```

Behavior:

- Message phai ton tai va chua bi current user soft-delete.
- Current user phai la member cua conversation chua message.
- Moi conversation toi da 5 pinned messages.
- Neu message da duoc pin, tra lai message response hien tai.

Response `data`: `MessageResponse` co them:

```json
{
  "is_pinned": true,
  "pinned_by": "userId",
  "pinned_at": "2026-06-02T00:00:00"
}
```

### Unpin Message

```http
DELETE /messages/{messageId}/pin
```

Behavior:

- Message phai ton tai.
- Current user phai la member cua conversation chua message.
- Xoa pinned state neu co.

### Get Pinned Messages

```http
GET /messages/conversation/{conversationId}/pinned
```

Behavior:

- Current user phai la member cua conversation.
- Tra pinned messages theo `pinned_at DESC`.
- Khong tra message da bi current user soft-delete.

Response `data`: `List<MessageResponse>`.

### Get Conversation Images

```http
GET /messages/conversation/{conversationId}/media/images?page=0&size=20
```

Behavior:

- Current user phai la member cua conversation.
- Chi tra visible messages type `IMAGE`.
- Exclude messages da bi current user soft-delete.
- Sort theo `createdAt DESC`.

Response `data`: `Page<MessageResponse>`.

### Get Conversation Image Preview

```http
GET /messages/conversation/{conversationId}/media/images/preview?limit=3
```

Behavior:

- Current user phai la member cua conversation.
- Tra anh visible moi nhat type `IMAGE`.
- `limit` default la 3, service clamp trong khoang 1 den 20.
- Sort theo `createdAt DESC`.

Response `data`: `List<MessageResponse>`.

### Get Conversation Files

```http
GET /messages/conversation/{conversationId}/media/files?page=0&size=20
```

Behavior:

- Current user phai la member cua conversation.
- Chi tra visible messages type `FILE`.
- Exclude messages da bi current user soft-delete.
- Sort theo `createdAt DESC`.

Response `data`: `Page<MessageResponse>`.

### Get Conversation Links

```http
GET /messages/conversation/{conversationId}/media/links?page=0&size=20
```

Behavior:

- Current user phai la member cua conversation.
- Scan visible text messages co kha nang chua link.
- Parse link trong service, ho tro `https://facebook.com`, `www.facebook.com`, `facebook.com`, `facebook.com/path`.
- Khong bat email fragment nhu `gmail.com` trong `abc@gmail.com`.
- Trim dau cau cuoi link, vi du `facebook.com.` thanh `facebook.com`.
- Sort theo message `createdAt DESC`; neu mot message co nhieu link, tra tung link thanh mot item.

Response `data`: `Page<ConversationLinkResponse>`.

ConversationLinkResponse:

```json
{
  "message_id": "messageId",
  "conversation_id": "conversationId",
  "sender_id": "senderId",
  "url": "facebook.com",
  "normalized_url": "https://facebook.com",
  "text": "hom nay toi xem facebook.com hay lam",
  "created_at": "2026-06-02T00:00:00"
}
```
