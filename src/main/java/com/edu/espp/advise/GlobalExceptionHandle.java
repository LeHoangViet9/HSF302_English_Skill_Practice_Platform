package com.edu.espp.advise;

import com.edu.espp.common.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandle {

    // Giữ lại hàm này để trả về giao diện lỗi 404 cho người dùng xem
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFoundException(NoResourceFoundException exception, Model model) {
        log.warn("Không tìm thấy tài nguyên tĩnh: {}", exception.getResourcePath());

        model.addAttribute("title", "Tài Nguyên Không Tồn Tại");
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        model.addAttribute("errorMessage", "Tài nguyên bạn yêu cầu hiện không có trên hệ thống.");
        return "error";
    }

    @ExceptionHandler(AppException.class)
    public String handleAppException(AppException exception, Model model) {
        model.addAttribute("title", "Lỗi Ứng Dụng");
        model.addAttribute("status", exception.getStatus().value());
        model.addAttribute("errorMessage", exception.getMessage());
        return "error";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationExceptions(MethodArgumentNotValidException exception, Model model) {
        String fieldErrorsMessage = exception.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        model.addAttribute("title", "Dữ Liệu Không Hợp Lệ");
        model.addAttribute("status", HttpStatus.BAD_REQUEST.value());
        model.addAttribute("errorMessage", "Chi tiết: " + fieldErrorsMessage);
        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
        String exceptionMsg = ex.getMessage();
        String finalMessage = exceptionMsg;

        if (exceptionMsg != null && exceptionMsg.matches("\\d+")) {
            finalMessage = "Thời gian làm bài bị vượt quá giới hạn: " + exceptionMsg + " phút";
        }

        model.addAttribute("title", "Yêu Cầu Không Hợp Lệ");
        model.addAttribute("status", HttpStatus.BAD_REQUEST.value());
        model.addAttribute("errorMessage", finalMessage);
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception exception, Model model) {
        log.error("Unhandled exception", exception);

        model.addAttribute("title", "Lỗi Hệ Thống");
        model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("errorMessage", "Hệ thống gặp sự cố, vui lòng thử lại sau.");
        return "error";
    }
}