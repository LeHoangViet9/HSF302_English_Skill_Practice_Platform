package com.edu.espp.advise;

import com.edu.espp.common.dto.response.ApiResponse;
import com.edu.espp.common.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandle {

    @Autowired
    private MessageSource messageSource;

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
        Locale locale = LocaleContextHolder.getLocale();

        String fieldErrorsMessage = exception.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        String validationFailedMessage = messageSource.getMessage(
                "Dữ liệu không hợp lệ",
                null,
                "Dữ liệu không hợp lệ: " + fieldErrorsMessage,
                locale
        );

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message(validationFailedMessage)
                .error(fieldErrorsMessage)
                .status(HttpStatus.BAD_REQUEST)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        Locale locale = LocaleContextHolder.getLocale();

        String message = messageSource.getMessage(
                "Lỗi hệ thống",
                null,
                "Hệ thống gặp sự cố, vui lòng thử lại sau",
                locale
        );

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message(message)
                .error(exception.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Locale locale = LocaleContextHolder.getLocale();
        String exceptionMsg = ex.getMessage();
        String finalMessage = exceptionMsg;

        if (exceptionMsg != null && exceptionMsg.matches("\\d+")) {
            finalMessage = messageSource.getMessage(
                    "Thời gian làm bài bị vươt quá",
                    new Object[]{exceptionMsg},
                    "Thời gian làm bài bị vượt quá giới hạn: " + exceptionMsg + " phút",
                    locale
            );
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
        Locale locale = LocaleContextHolder.getLocale();

        String message = messageSource.getMessage(
                "error.access.denied",
                null,
                "Access denied - Bạn không có quyền truy cập chức năng này",
                locale
        );

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message(message)
                .error("Access denied")
                .status(HttpStatus.FORBIDDEN)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}