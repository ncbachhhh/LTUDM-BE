package com.ncbachhhh.LTUDM.exception;

import lombok.Getter;
import lombok.Setter;

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
    INVALID_DISPLAY_NAME_FORMAT(400, "Display name must be at most 30 characters long"),
    USER_BANNED(403, "User is banned"),
    WRONG_OLD_PASSWORD(401, "Password is incorrect"),

    // ===== Server errors =====
    INTERNAL_ERROR(500, "Internal server error"),
    DATABASE_ERROR(500, "Database error"),

    // ===== Unknown errors =====
    UNCATEGORIZED_EXCEPTION(500, "Uncategorized exception");

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;


}
