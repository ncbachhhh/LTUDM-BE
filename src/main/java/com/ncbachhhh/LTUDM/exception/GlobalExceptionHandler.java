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
    // Fallback cho lỗi chưa được phân loại; log stacktrace để debug production.
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<?>> handleRuntimeException(Exception exception) {
        log.error("Unhandled exception", exception);
        return error(ErrorCode.UNCATEGORIZED_EXCEPTION, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Map lỗi authorization của Spring Security thành response 403 thống nhất.
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException exception) {
        return error(ErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN);
    }

    // Map lỗi database về error code chung và log chi tiết ở server.
    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<ApiResponse<?>> handleDataAccessException(DataAccessException exception) {
        log.error("Database exception", exception);
        return error(ErrorCode.DATABASE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Lỗi nghiệp vụ có ErrorCode sẵn, status HTTP được suy ra từ numeric code.
    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse<?>> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return error(errorCode, statusOf(errorCode));
    }

    // Lỗi validation DTO: defaultMessage được dat bằng tên ErrorCode trong annotation.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException exception) {
        return error(resolveValidationError(exception), HttpStatus.BAD_REQUEST);
    }

    // Lỗi upload vượt max request size của Spring multipart.
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiResponse<?>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException exception) {
        return error(ErrorCode.AVATAR_FILE_TOO_LARGE, HttpStatus.BAD_REQUEST);
    }

    // Chuyển validation message thanh ErrorCode; fallback VALIDATION_FAILED nếu message không khop enum.
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

    // Tạo response lỗi theo format ApiResponse chung.
    private ResponseEntity<ApiResponse<?>> error(ErrorCode errorCode, HttpStatus status) {
        ApiResponse<?> response = ApiResponse.error(errorCode.getCode(), errorCode.getMessage());
        return ResponseEntity.status(status).body(response);
    }

    // Suy ra HTTP status từ numeric code trong ErrorCode.
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
