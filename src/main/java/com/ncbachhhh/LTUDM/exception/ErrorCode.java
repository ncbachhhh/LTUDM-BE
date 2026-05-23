package com.ncbachhhh.LTUDM.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(200, "Success"),

    BAD_REQUEST(400, "Bad request"),
    VALIDATION_FAILED(400, "Validation failed"),

    UNAUTHENTICATED(401, "Authentication is required"),
    ACCESS_DENIED(403, "Access denied"),
    USER_NOT_FOUND(404, "User not found"),
    WRONG_PASSWORD(401, "Email or password is incorrect"),
    EMAIL_NOT_FOUND(404, "Email not found"),
    EMAIL_ALREADY_EXISTS(400, "Email already exists"),
    USERNAME_ALREADY_EXISTS(400, "Username already exists"),
    INVALID_EMAIL_FORMAT(400, "Email format is invalid"),
    INVALID_USERNAME_FORMAT(400, "Username must contain 3 to 20 characters"),
    INVALID_PASSWORD_FORMAT(400, "Password must contain at least 8 characters"),
    INVALID_DISPLAY_NAME_FORMAT(400, "Display name must not exceed 100 characters"),
    USER_BANNED(403, "Account has been banned"),
    WRONG_OLD_PASSWORD(401, "Old password is incorrect"),
    PASSWORD_NOT_MATCH(400, "Password confirmation does not match"),
    SAME_PASSWORD(400, "New password must be different from current password"),
    INVALID_TOKEN(401, "Token is invalid or expired"),
    TOKEN_REQUIRED(400, "Token is required"),
    REFRESH_TOKEN_REQUIRED(400, "Refresh token is required"),
    OLD_PASSWORD_REQUIRED(400, "Old password is required"),
    NEW_PASSWORD_REQUIRED(400, "New password is required"),
    CONFIRM_PASSWORD_REQUIRED(400, "Password confirmation is required"),
    INVALID_RESET_OTP(400, "OTP is invalid or expired"),
    RESET_OTP_TOO_MANY_ATTEMPTS(400, "Too many invalid OTP attempts"),
    INVALID_RESET_TOKEN(400, "Password reset session is invalid or expired"),
    AVATAR_FILE_REQUIRED(400, "Avatar file is required"),
    INVALID_AVATAR_FILE_TYPE(400, "Avatar must be JPG, PNG, GIF, or WEBP"),
    AVATAR_FILE_TOO_LARGE(400, "Avatar must not exceed 5MB"),
    AVATAR_UPLOAD_FAILED(500, "Avatar upload failed"),
    INVALID_AVATAR_URL(400, "Avatar URL is invalid"),
    AVATAR_URL_TOO_LONG(400, "Avatar URL must not exceed 500 characters"),
    SEARCH_QUERY_REQUIRED(400, "Search query is required"),

    CANNOT_FRIEND_SELF(400, "Cannot send a friend request to yourself"),
    FRIENDSHIP_ALREADY_EXISTS(400, "Friendship request already exists"),
    FRIENDSHIP_NOT_FOUND(404, "Friendship request not found"),
    FRIENDSHIP_REQUEST_NOT_PENDING(400, "Friendship request is not pending"),
    FRIENDSHIP_REQUEST_NOT_RECEIVED(403, "You cannot handle this friendship request"),
    USER_NOT_BLOCKED(400, "User is not blocked"),
    NOT_FRIENDS(403, "Users are not friends"),

    MESSAGE_NOT_FOUND(404, "Message not found"),
    CONVERSATION_NOT_FOUND(404, "Conversation not found"),
    NOT_CONVERSATION_MEMBER(403, "User is not a member of this conversation"),
    INVALID_CONVERSATION_TYPE(400, "Conversation type must be DIRECT or GROUP"),
    INVALID_DIRECT_CONVERSATION_MEMBERS(400, "Direct conversation must contain exactly one other user"),
    INVALID_GROUP_CONVERSATION_MEMBERS(400, "Group conversation must contain at least two members"),
    INVALID_CONVERSATION_MEMBERS(400, "Conversation members are invalid"),
    INVALID_NICKNAME_FORMAT(400, "Nickname must not exceed 100 characters"),
    GROUP_TITLE_REQUIRED(400, "Group title is required"),
    MEMBER_ALREADY_IN_CONVERSATION(400, "All users are already members of this conversation"),
    GROUP_OPERATION_NOT_ALLOWED(400, "This operation is only allowed for group conversations"),
    NOT_GROUP_MANAGER(403, "User is not allowed to manage this group"),
    EMPTY_MESSAGE(400, "Message content must not be empty"),
    MESSAGE_IMAGE_FILE_REQUIRED(400, "Image message requires an image file"),
    INVALID_MESSAGE_IMAGE_FILE_TYPE(400, "Message image must be JPG, PNG, GIF, or WEBP"),
    MESSAGE_IMAGE_FILE_TOO_LARGE(400, "Message image must not exceed 10MB"),
    MESSAGE_IMAGE_UPLOAD_FAILED(500, "Message image upload failed"),
    MESSAGE_FILE_REQUIRED(400, "File message requires an attachment"),
    INVALID_MESSAGE_FILE_TYPE(400, "Attachment file type is not supported"),
    MESSAGE_FILE_TOO_LARGE(400, "Attachment must not exceed 100MB"),
    MESSAGE_FILE_UPLOAD_FAILED(500, "Attachment upload failed"),
    IMAGE_MESSAGE_NOT_SUPPORTED_OVER_WEBSOCKET(400, "Image messages must be sent with multipart HTTP request"),

    INTERNAL_ERROR(500, "Internal server error"),
    DATABASE_ERROR(500, "Database error"),
    UNCATEGORIZED_EXCEPTION(500, "Uncategorized error");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
