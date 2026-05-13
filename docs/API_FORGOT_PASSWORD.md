# API Quên Mật Khẩu

Base URL:

```text
http://localhost:8080/api/v1
```

Luồng quên mật khẩu gồm 3 bước:

1. Gửi OTP về email.
2. Xác thực OTP để nhận `resetToken`.
3. Dùng `resetToken` để đổi mật khẩu mới.

Frontend không kết nối Redis trực tiếp. Redis chỉ được backend dùng để lưu OTP, cooldown, số lần nhập sai và reset token tạm thời.

## 1. Gửi OTP quên mật khẩu

```http
POST /auth/forgot-password
```

Request:

```json
{
  "email": "test1@gmail.com"
}
```

Response:

```json
{
  "code": 200,
  "message": "Nếu email tồn tại, mã OTP đã được gửi."
}
```

Ghi chú:

- Response luôn giống nhau, kể cả khi email không tồn tại.
- Email bắt buộc không rỗng và đúng format.
- Nếu email tồn tại, backend tạo OTP 6 chữ số và gửi qua email.
- OTP có hiệu lực 5 phút.
- Không gửi lại OTP trong vòng 60 giây.
- OTP được hash bằng BCrypt trước khi lưu Redis.
- Không log OTP.

## 2. Xác thực OTP

```http
POST /auth/verify-reset-otp
```

Request:

```json
{
  "email": "test1@gmail.com",
  "otp": "123456"
}
```

Response thành công:

```json
{
  "code": 200,
  "message": "Xác thực OTP thành công.",
  "data": {
    "resetToken": "random-reset-token"
  }
}
```

Response thất bại:

```json
{
  "code": 400,
  "message": "OTP không hợp lệ hoặc đã hết hạn."
}
```

Ghi chú:

- OTP phải gồm đúng 6 chữ số.
- OTP chỉ dùng được một lần.
- Nhập sai tối đa 5 lần.
- Sau khi OTP đúng, backend xóa OTP cũ và tạo `resetToken`.
- `resetToken` có hiệu lực 10 phút.
- Frontend lưu tạm `resetToken` để gọi API đổi mật khẩu.

## 3. Đổi mật khẩu bằng resetToken

```http
POST /auth/reset-password
```

Request:

```json
{
  "resetToken": "paste-reset-token-from-step-2",
  "newPassword": "newPassword123"
}
```

Response thành công:

```json
{
  "code": 200,
  "message": "Đổi mật khẩu thành công."
}
```

Response thất bại:

```json
{
  "code": 400,
  "message": "Phiên đặt lại mật khẩu không hợp lệ hoặc đã hết hạn."
}
```

Ghi chú:

- `resetToken` bắt buộc không rỗng.
- `newPassword` tối thiểu 8 ký tự.
- API này không nhận email trực tiếp.
- Backend lấy email từ Redis thông qua `resetToken`.
- Mật khẩu mới được hash bằng BCrypt.
- `resetToken` bị xóa sau khi đổi mật khẩu thành công.
- Backend không tự động đăng nhập user sau khi đổi mật khẩu.

## Redis

| Key | Giá trị | TTL |
|-----|---------|-----|
| `forgot:otp:{email}` | OTP đã hash bằng BCrypt | 5 phút |
| `forgot:cooldown:{email}` | Cờ chặn gửi lại OTP | 60 giây |
| `forgot:attempts:{email}` | Số lần nhập sai OTP | 5 phút |
| `forgot:reset-token:{sha256(resetToken)}` | Email tương ứng với reset token | 10 phút |

## Thứ tự test bằng Postman

1. Gọi `POST /auth/forgot-password`.
2. Mở email để lấy OTP.
3. Gọi `POST /auth/verify-reset-otp`.
4. Copy `data.resetToken`.
5. Gọi `POST /auth/reset-password`.
6. Đăng nhập lại bằng mật khẩu mới.

## Biến môi trường

```env
REDIS_HOST=redis
REDIS_PORT=6379

MAIL_USERNAME=your_gmail_address@gmail.com
MAIL_PASSWORD=your_gmail_app_password
```

Nếu chạy backend local ngoài Docker, `REDIS_HOST` thường là `localhost`.

Nếu chạy bằng Docker Compose, `REDIS_HOST` là `redis`.

Gmail phải dùng App Password, không dùng mật khẩu Gmail thường.
