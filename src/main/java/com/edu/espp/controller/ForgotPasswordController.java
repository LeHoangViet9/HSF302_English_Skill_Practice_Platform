package com.edu.espp.controller;

import com.edu.espp.dto.ForgotPasswordForm;
import com.edu.espp.service.ForgotPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(@RequestParam(required = false) String sent, Model model) {
        if (!model.containsAttribute("forgotPasswordForm")) {
            model.addAttribute("forgotPasswordForm", new ForgotPasswordForm());
        }
        if ("true".equals(sent)) {
            model.addAttribute("sent", true);
        }
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @ModelAttribute("forgotPasswordForm") ForgotPasswordForm form,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "forgot-password";
        }

        ForgotPasswordService.PasswordResetRequestResult result = forgotPasswordService
                .requestPasswordReset(form.getEmail());
        if (result.rateLimited()) {
            model.addAttribute("rateLimitError",
                    "Quá nhiều yêu cầu vui lòng đăng nhập lại sau" + result.retryAfterMinutes() + " phÃºt.");
            return "forgot-password";
        }

        return "redirect:/forgot-password?sent=true";
    }
}
