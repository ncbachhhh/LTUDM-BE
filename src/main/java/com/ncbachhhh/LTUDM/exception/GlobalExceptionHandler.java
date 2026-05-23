package com.ncbachhhh.LTUDM.exception;

import com.ncbachhhh.LTUDM.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<?>> handleRuntimeException(Exception exception) {
        log.error("Unhandled exception", exception);
        return error(ErrorCode.UNCATEGORIZED_EXCEPTION, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException exception) {
        return error(ErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<ApiResponse<?>> handleDataAccessException(DataAccessException exception) {
        log.error("Database exception", exception);
        return error(ErrorCode.DATABASE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse<?>> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return error(errorCode, statusOf(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException exception) {
        return error(resolveValidationError(exception), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiResponse<?>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException exception) {
        return error(ErrorCode.AVATAR_FILE_TOO_LARGE, HttpStatus.BAD_REQUEST);
    }

    private ErrorCode resolveValidationError(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError() == null
                ? null
                : exception.getFieldError().getDefaultMessage();

        try {
            return enumKey == null ? ErrorCode.VALIDATION_FAILED : ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException ignored) {
            return ErrorCode.VALIDATION_FAILED;
        }
    }

    private ResponseEntity<ApiResponse<?>> error(ErrorCode errorCode, HttpStatus status) {
        ApiResponse<?> response = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(status).body(response);
    }

    private HttpStatus statusOf(ErrorCode errorCode) {
        return switch (errorCode.getCode()) {
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            case 500 -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
