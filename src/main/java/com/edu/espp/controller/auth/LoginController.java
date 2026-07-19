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
    public String showLoginPage(
            @RequestParam(name = "error", required = false) String error,

            @RequestParam(name = "registered", required = false) String registered,

            @RequestParam(name = "logout", required = false) String logout,

            @RequestParam(name = "reset", required = false) String reset,

            Model model) {

        if (error != null) {
            model.addAttribute("error", "Email hoặc mật khẩu không đúng");
        }

        if (registered != null) {
            model.addAttribute("successMsg", "Đăng ký thành công. Vui lòng đăng nhập.");
        } else if (reset != null) {
            model.addAttribute("successMsg", "Đặt lại mật khẩu thành công. " + "Vui lòng đăng nhập bằng mật khẩu mới.");
        } else if (logout != null) {
            model.addAttribute("successMsg", "Bạn đã đăng xuất thành công.");
        }

        return "auth/login";
    }
}
