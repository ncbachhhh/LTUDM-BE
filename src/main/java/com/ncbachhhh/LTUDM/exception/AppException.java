package com.ncbachhhh.LTUDM.exception;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AppException extends RuntimeException {
    private ErrorCode errorCode;

    // Exception nghiệp vụ mang theo ErrorCode để GlobalExceptionHandler map thành ApiResponse.
    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
