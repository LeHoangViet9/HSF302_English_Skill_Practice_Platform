package com.edu.espp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String success,
            Model model
    ) {
        if (success != null) {
            model.addAttribute("successMsg", success);
        }

        if (error != null) {
            model.addAttribute("error", resolveLoginError(error));
        }

        return "login";
    }

    private String resolveLoginError(String errorCode) {
        return switch (errorCode) {
            case "EMAIL_NOT_VERIFIED" -> "Email của bạn chưa được xác minh. Vui lòng kiểm tra hộp thư.";
            case "ACCOUNT_SUSPENDED" -> "Tài khoản của bạn đang bị khóa hoặc tạm ngưng.";
            default -> "Email hoặc mật khẩu không đúng.";
        };
    }
}
