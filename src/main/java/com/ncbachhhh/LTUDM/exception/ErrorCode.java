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
    USER_NOT_FOUND(404, "User not found"),
    WRONG_PASSWORD(401, "Wrong password"),
    EMAIL_ALREADY_EXISTS(400, "Email already exists"),

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
