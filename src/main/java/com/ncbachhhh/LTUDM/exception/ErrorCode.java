package com.ncbachhhh.LTUDM.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // ===== Success =====
    SUCCESS(200, "Thành công"),

    // ===== Client errors =====
    BAD_REQUEST(400, "Yêu cầu không hợp lệ"),
    VALIDATION_FAILED(400, "Dữ liệu không hợp lệ"),

    // ===== Auth errors =====
    UNAUTHENTICATED(401, "Chưa đăng nhập"),
    ACCESS_DENIED(403, "Không có quyền truy cập tài nguyên này"),
    USER_NOT_FOUND(404, "Không tìm thấy người dùng"),
    WRONG_PASSWORD(401, "Email hoặc mật khẩu không đúng"),
    EMAIL_NOT_FOUND(404, "Không tìm thấy email"),
    EMAIL_ALREADY_EXISTS(400, "Email đã tồn tại"),
    USERNAME_ALREADY_EXISTS(400, "Username đã tồn tại"),
    INVALID_EMAIL_FORMAT(400, "Email không đúng định dạng"),
    INVALID_USERNAME_FORMAT(400, "Username phải có từ 3 đến 20 ký tự"),
    INVALID_PASSWORD_FORMAT(400, "Mật khẩu phải có ít nhất 8 ký tự"),
    INVALID_DISPLAY_NAME_FORMAT(400, "Tên hiển thị không được vượt quá 100 ký tự"),
    USER_BANNED(403, "Tài khoản đã bị khóa"),
    WRONG_OLD_PASSWORD(401, "Mật khẩu không đúng"),
    PASSWORD_NOT_MATCH(400, "Xác nhận mật khẩu không khớp"),
    SAME_PASSWORD(400, "Mật khẩu mới phải khác mật khẩu hiện tại"),
    INVALID_TOKEN(401, "Token không hợp lệ hoặc đã hết hạn"),
    TOKEN_REQUIRED(400, "Token là bắt buộc"),
    REFRESH_TOKEN_REQUIRED(400, "Refresh token là bắt buộc"),
    OLD_PASSWORD_REQUIRED(400, "Mật khẩu cũ là bắt buộc"),
    NEW_PASSWORD_REQUIRED(400, "Mật khẩu mới là bắt buộc"),
    CONFIRM_PASSWORD_REQUIRED(400, "Xác nhận mật khẩu là bắt buộc"),
    INVALID_RESET_OTP(400, "OTP không hợp lệ hoặc đã hết hạn."),
    RESET_OTP_TOO_MANY_ATTEMPTS(400, "Bạn đã nhập sai OTP quá nhiều lần. Vui lòng thử lại sau."),
    INVALID_RESET_TOKEN(400, "Phiên đặt lại mật khẩu không hợp lệ hoặc đã hết hạn."),
    AVATAR_FILE_REQUIRED(400, "Vui lòng chọn file avatar"),
    INVALID_AVATAR_FILE_TYPE(400, "Avatar phải là ảnh JPG, PNG, GIF hoặc WEBP"),
    AVATAR_FILE_TOO_LARGE(400, "Dung lượng avatar không được vượt quá 5MB"),
    AVATAR_UPLOAD_FAILED(500, "Upload avatar thất bại"),
    INVALID_AVATAR_URL(400, "Avatar URL không hợp lệ"),
    AVATAR_URL_TOO_LONG(400, "Avatar URL không được vượt quá 500 ký tự"),

    // ===== Message errors =====
    MESSAGE_NOT_FOUND(404, "Không tìm thấy tin nhắn"),
    CONVERSATION_NOT_FOUND(404, "Không tìm thấy đoạn chat"),
    NOT_CONVERSATION_MEMBER(403, "Bạn không phải thành viên của đoạn chat này"),
    INVALID_CONVERSATION_TYPE(400, "Loại đoạn chat phải là DIRECT hoặc GROUP"),
    INVALID_DIRECT_CONVERSATION_MEMBERS(400, "Chat cá nhân phải có đúng một người dùng khác"),
    INVALID_GROUP_CONVERSATION_MEMBERS(400, "Chat nhóm phải có ít nhất hai thành viên"),
    INVALID_CONVERSATION_MEMBERS(400, "Danh sách thành viên đoạn chat không hợp lệ"),
    GROUP_TITLE_REQUIRED(400, "Tên nhóm là bắt buộc"),
    MEMBER_ALREADY_IN_CONVERSATION(400, "Tất cả người dùng đã là thành viên của đoạn chat này"),
    GROUP_OPERATION_NOT_ALLOWED(400, "Thao tác này chỉ áp dụng cho chat nhóm"),
    NOT_GROUP_MANAGER(403, "Bạn không có quyền quản lý nhóm này"),
    EMPTY_MESSAGE(400, "Nội dung tin nhắn không được để trống"),
    MESSAGE_IMAGE_FILE_REQUIRED(400, "Tin nhắn ảnh cần có file ảnh"),
    INVALID_MESSAGE_IMAGE_FILE_TYPE(400, "Ảnh tin nhắn phải là JPG, PNG, GIF hoặc WEBP"),
    MESSAGE_IMAGE_FILE_TOO_LARGE(400, "Dung lượng ảnh tin nhắn không được vượt quá 10MB"),
    MESSAGE_IMAGE_UPLOAD_FAILED(500, "Upload ảnh tin nhắn thất bại"),
    IMAGE_MESSAGE_NOT_SUPPORTED_OVER_WEBSOCKET(400, "Tin nhắn ảnh phải được gửi bằng multipart HTTP request"),

    // ===== Server errors =====
    INTERNAL_ERROR(500, "Lỗi máy chủ"),
    DATABASE_ERROR(500, "Lỗi cơ sở dữ liệu"),

    // ===== Unknown errors =====
    UNCATEGORIZED_EXCEPTION(500, "Lỗi không xác định");

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private final int code;
    private final String message;


}
