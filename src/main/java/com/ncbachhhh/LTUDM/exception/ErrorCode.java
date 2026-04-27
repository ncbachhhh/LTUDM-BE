package com.ncbachhhh.LTUDM.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // ===== Success =====
    SUCCESS(200, "Success"),

    // ===== Client errors =====
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Resource not found"),
    VALIDATION_FAILED(400, "Validation failed"),

    // ===== Auth errors =====
    UNAUTHENTICATED(401, "Unauthenticated"),
    ACCESS_DENIED(403, "No permission to access this resource"),
    USER_NOT_FOUND(404, "User not found"),
    WRONG_PASSWORD(401, "Wrong password or email"),
    EMAIL_NOT_FOUND(404, "Email not found"),
    EMAIL_ALREADY_EXISTS(400, "Email already exists"),
    USERNAME_ALREADY_EXISTS(400, "Username already exists"),
    INVALID_EMAIL_FORMAT(400, "Invalid email format"),
    INVALID_USERNAME_FORMAT(400, "Username must be between 3 and 20 characters"),
    INVALID_PASSWORD_FORMAT(400, "Password must be at least 8 characters long"),
    INVALID_DISPLAY_NAME_FORMAT(400, "Display name must be at most 100 characters long"),
    USER_BANNED(403, "User is banned"),
    WRONG_OLD_PASSWORD(401, "Password is incorrect"),
    PASSWORD_NOT_MATCH(400, "Password confirmation does not match"),
    SAME_PASSWORD(400, "New password must be different from current password"),
    INVALID_TOKEN(401, "Token is invalid or expired"),
    AVATAR_FILE_REQUIRED(400, "Avatar file is required"),
    INVALID_AVATAR_FILE_TYPE(400, "Avatar must be a JPG, PNG, GIF, or WEBP image"),
    AVATAR_FILE_TOO_LARGE(400, "Avatar file size must not exceed 5MB"),
    AVATAR_UPLOAD_FAILED(500, "Failed to upload avatar"),

    // ===== Message errors =====
    MESSAGE_NOT_FOUND(404, "Message not found"),
    CONVERSATION_NOT_FOUND(404, "Conversation not found"),
    NOT_CONVERSATION_MEMBER(403, "You are not a member of this conversation"),
    INVALID_CONVERSATION_TYPE(400, "Conversation type must be DIRECT or GROUP"),
    INVALID_DIRECT_CONVERSATION_MEMBERS(400, "Direct conversation must contain exactly one other user"),
    INVALID_GROUP_CONVERSATION_MEMBERS(400, "Group conversation must contain at least two members"),
    INVALID_CONVERSATION_MEMBERS(400, "Conversation members are invalid"),
    GROUP_TITLE_REQUIRED(400, "Group conversation title is required"),
    MEMBER_ALREADY_IN_CONVERSATION(400, "All users are already members of this conversation"),
    GROUP_OPERATION_NOT_ALLOWED(400, "This operation is only allowed for group conversations"),
    NOT_GROUP_MANAGER(403, "You do not have permission to manage this group"),
    EMPTY_MESSAGE(400, "Message content cannot be empty"),
    MESSAGE_IMAGE_FILE_REQUIRED(400, "Image message requires an image file"),
    INVALID_MESSAGE_IMAGE_FILE_TYPE(400, "Message image must be a JPG, PNG, GIF, or WEBP image"),
    MESSAGE_IMAGE_FILE_TOO_LARGE(400, "Message image size must not exceed 10MB"),
    MESSAGE_IMAGE_UPLOAD_FAILED(500, "Failed to upload message image"),
    IMAGE_MESSAGE_NOT_SUPPORTED_OVER_WEBSOCKET(400, "Image messages must be sent via multipart HTTP request"),

    // ===== Server errors =====
    INTERNAL_ERROR(500, "Internal server error"),
    DATABASE_ERROR(500, "Database error"),

    // ===== Unknown errors =====
    UNCATEGORIZED_EXCEPTION(500, "Uncategorized exception");

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private final int code;
    private final String message;


}
