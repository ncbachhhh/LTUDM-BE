# Business Flows

## Authentication Flow

1. Register tao user va hash password.
2. Login xac thuc email/password, kiem tra `active`, tao access token va refresh token.
3. Access token duoc dung cho REST API va STOMP CONNECT.
4. Refresh token tao cap token moi va revoke refresh token cu bang `jti`.
5. Logout revoke token duoc gui len.

JWT dung HS256 voi secret tu `jwt.secret`. Claim `scope` co format `ROLE_<role>`.

## Password Reset Flow

1. Client goi forgot password voi email.
2. Backend normalize email ve lowercase.
3. Neu user ton tai va khong cooldown, backend tao OTP 6 so.
4. OTP duoc hash va luu Redis key `forgot:otp:{email}` trong 5 phut.
5. Attempts key `forgot:attempts:{email}` duoc reset.
6. Cooldown key `forgot:cooldown:{email}` duoc set 60 giay.
7. EmailService gui OTP qua SMTP.
8. Verify OTP doc hash tu Redis, so sanh bang password encoder.
9. Sai OTP tang attempts. Qua 5 lan thi OTP bi xoa.
10. Dung OTP tao reset token random, luu Redis key `forgot:reset-token:{sha256(token)}` trong 10 phut.
11. Reset password doc email tu reset token key, update password hash, xoa Redis keys lien quan.

## Relationship State Flow

`RelationshipService` la nguon chinh tinh trang quan he giua 2 user.

Order resolve:

1. Neu current user block other user trong bang `blocks`, status `BLOCKED`, direction `OUTGOING`.
2. Neu other user block current user, status `BLOCKED`, direction `INCOMING`.
3. Neu co legacy friendship `BLOCKED`, status `BLOCKED`, direction theo requester.
4. Neu co friendship `PENDING`, direction la `OUTGOING` neu current user la requester, nguoc lai `INCOMING`.
5. Neu co friendship `ACCEPTED`, direction `NONE`.
6. Neu khong co relationship, status `NONE`, direction `NONE`.

Service nay duoc dung boi user profile, friendship response, conversation preview va message sending guard.

## Friendship Flow

Send request:

1. Validate target user ton tai, active, khong phai current user.
2. Neu friendship cu la `DECLINED`, reuse record va set `PENDING`.
3. Neu relationship khac da ton tai, tra loi.
4. Tao friendship `PENDING`.

Accept request:

1. Current user phai la addressee.
2. Request phai `PENDING`.
3. Set `ACCEPTED`.
4. Tim hoac tao direct conversation.

Block:

1. Tao record `Block(currentUserId, targetUserId)`.
2. Neu co legacy blocked friendship do current user tao, restore thanh `ACCEPTED`.
3. Relationship state se uu tien bang `blocks`, nen API va message guard deu thay status `BLOCKED`.

Unblock:

1. Xoa record trong bang `blocks`.
2. Neu co legacy blocked friendship, restore thanh `ACCEPTED`.
3. Neu khong co block nao, tra `USER_NOT_BLOCKED`.

## Conversation Flow

Direct conversation:

1. Request phai co type `DIRECT`.
2. Request phai co exactly one target member.
3. Current user khong duoc tao direct conversation voi chinh minh.
4. Target user phai ton tai.
5. Relationship phai accepted, khong block.
6. Neu da co direct conversation giua 2 user, tra conversation do.
7. Neu chua co, tao conversation type `DIRECT`, createdBy current user.
8. Tao 2 `ConversationMember`: owner la current user, member la target user.

Group conversation:

1. Request phai co type `GROUP`.
2. Title bat buoc.
3. Backend trim, dedupe member IDs, them current user.
4. Tong members phai >= 2.
5. Current user phai accepted friend voi tung member duoc them.
6. Tao conversation type `GROUP`, createdBy current user.
7. Current user role `OWNER`, user khac role `MEMBER`.

Conversation preview:

1. Load members va user profile.
2. Gan online state cho moi member tu `PresenceService`.
3. Count unread messages.
4. Lay latest visible message.
5. Neu direct conversation, gan relationship state.
6. Sort conversations theo latest message time, fallback created time.

## Message Flow

Text message:

1. Sender lay tu JWT.
2. Sender phai la conversation member.
3. Direct conversation phai la accepted friends.
4. File part khong duoc gui voi type `TEXT`.
5. Content trim khong duoc empty.
6. Save message.
7. REST/WebSocket controller broadcast message va preview.

Image/file message:

1. Sender phai la member va duoc phep gui.
2. File bat buoc.
3. Validate content type va size.
4. Upload len R2.
5. Save URL vao `Message.content`.
6. Save attachment metadata vao `attachments`.
7. Broadcast nhu text message.

Read flow:

1. Mark single message read tao receipt neu user khong phai sender.
2. Mark all read tao receipt cho visible messages chua read.
3. Receipt duoc dung de tinh `is_read` va unread count.

Delete flow:

1. Delete message la soft delete theo user.
2. Backend tao `MessageDeletion(messageId, userId)`.
3. Query visible message exclude message da co deletion cua user do.

## Presence Flow

1. WebSocket CONNECT hop le mark online theo session id va user id.
2. Moi user co the co nhieu session.
3. User chi offline khi tat ca session da disconnect.
4. Khi online/offline thay doi, backend publish `PresenceResponse` den `/topic/presence`.
