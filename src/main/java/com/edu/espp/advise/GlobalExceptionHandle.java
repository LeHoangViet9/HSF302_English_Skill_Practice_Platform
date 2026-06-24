package com.edu.espp.advise;

import com.edu.espp.common.dto.response.ApiResponse;
import com.edu.espp.common.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandle {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException exception) {
        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message(exception.getMessage())
                .error(exception.getMessage())
                .status(exception.getStatus())
                .build();

        return ResponseEntity.status(exception.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException exception) {
        String fieldErrorsMessage = exception.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message("Dữ liệu không hợp lệ: " + fieldErrorsMessage)
                .error(fieldErrorsMessage)
                .status(HttpStatus.BAD_REQUEST)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        String exceptionMsg = ex.getMessage();
        String finalMessage = exceptionMsg;

        if (exceptionMsg != null && exceptionMsg.matches("\\d+")) {
            finalMessage = "Thời gian làm bài bị vượt quá giới hạn: " + exceptionMsg + " phút";
        }

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message(finalMessage)
                .error(exceptionMsg)
                .status(HttpStatus.BAD_REQUEST)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthorizationDenied(AuthorizationDeniedException exception) {
        log.warn("Authorization denied: {}", exception.getMessage());

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message("Bạn không có quyền truy cập chức năng này")
                .error(exception.getMessage())
                .status(HttpStatus.FORBIDDEN)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception exception) {
        log.error("Unhandled exception", exception);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message("Hệ thống gặp sự cố, vui lòng thử lại sau")
                .error(exception.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}