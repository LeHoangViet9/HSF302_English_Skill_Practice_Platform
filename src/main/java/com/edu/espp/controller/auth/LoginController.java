package com.edu.espp.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * POST /login is handled by Spring Security's
 * UsernamePasswordAuthenticationFilter
 * (see SecurityConfig), not this controller.
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
            @RequestParam(required = false) String verified,
            @RequestParam(required = false) String reset,
            Model model) {
        if (error != null) {
            model.addAttribute("error", resolveErrorMessage(error));
        }
        if ("true".equals(verified)) {
            model.addAttribute("successMsg", "Xác minh thành công! Bạn có thể đăng nhập.");
        }
        if ("true".equals(reset)) {
            model.addAttribute("successMsg", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập lại.");
        }
        return "auth/login";
    }

    private String resolveErrorMessage(String errorCode) {
        return switch (errorCode) {
            case "EMAIL_NOT_VERIFIED" -> "Tài khoản chưa xác minh email. Vui lòng kiểm tra hộp thư";
            case "ACCOUNT_SUSPENDED" -> "Tài khoản đã bị tạm khóa";
            default -> "Email hoặc mật khẩu không đúng";
        };
    }

}
