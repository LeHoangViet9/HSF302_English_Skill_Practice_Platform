package com.edu.espp.advise;

import com.edu.espp.common.exception.AppException;
//import org.springframework.security.authorization.AuthorizationDeniedException;
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

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFoundException(NoResourceFoundException exception, Model model) {
        // Log một dòng ngắn gọn ở chế độ WARN để mình biết nếu cần, không bắn exception đỏ lòm nữa
        log.warn("Không tìm thấy tài nguyên tĩnh: {}", exception.getResourcePath());

        model.addAttribute("title", "Tài Nguyên Không Tồn Tại");
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        model.addAttribute("errorMessage", "Tài nguyên bạn yêu cầu hiện không có trên hệ thống.");
        return "error";
    }

    // 1. Lỗi ứng dụng tự định nghĩa
    @ExceptionHandler(AppException.class)
    public String handleAppException(AppException exception, Model model) {
        model.addAttribute("title", "Lỗi Ứng Dụng");
        model.addAttribute("status", exception.getStatus().value());
        model.addAttribute("errorMessage", exception.getMessage());
        return "error"; // Trả về file templates/error.html
    }

    // 2. Lỗi Validate dữ liệu đầu vào
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

    // 3. Lỗi IllegalArgumentException (Ví dụ: quá giờ làm bài)
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

//     4. Lỗi Không có quyền truy cập
//    @ExceptionHandler(AuthorizationDeniedException.class)
//    public String handleAuthorizationDenied(AuthorizationDeniedException exception, Model model) {
//        log.warn("Authorization denied: {}", exception.getMessage());
//
//        model.addAttribute("title", "Truy Cập Bị Từ Chối");
//        model.addAttribute("status", HttpStatus.FORBIDDEN.value());
//        model.addAttribute("errorMessage", "Bạn không có quyền truy cập vào chức năng này.");
//        return "error";
//    }

    // 5. Lỗi không tìm thấy tài nguyên tĩnh (404) để tránh log lỗi hệ thống (như favicon.ico)
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        log.debug("Static resource not found: {}", ex.getResourcePath());
    }

    // 6. Lỗi hệ thống chung (500)
    @ExceptionHandler(Exception.class)
    public String handleException(Exception exception, Model model) {
        log.error("Unhandled exception", exception);

        model.addAttribute("title", "Lỗi Hệ Thống");
        model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("errorMessage", "Hệ thống gặp sự cố, vui lòng thử lại sau.");
        return "error";
    }
}