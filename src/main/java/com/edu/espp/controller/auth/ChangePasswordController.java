package com.edu.espp.controller.auth;

import com.edu.espp.dto.ResetPasswordForm;
import com.edu.espp.service.auth.ChangePasswordService;
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
public class ChangePasswordController {

    private final ChangePasswordService changePasswordService;

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String token, Model model) {
        if (token == null || token.isBlank() || !changePasswordService.isValidResetToken(token)) {
            model.addAttribute("invalidToken", true);
            return "auth/reset-password";
        }

        ResetPasswordForm form = new ResetPasswordForm();
        form.setToken(token);

        model.addAttribute("resetPasswordForm", form);
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute("resetPasswordForm") ResetPasswordForm form,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("token", form.getToken());
            return "auth/reset-password";
        }

        ChangePasswordService.ResetPasswordResult result = changePasswordService.resetPassword(form);

        if (result.invalidToken()) {
            model.addAttribute("invalidToken", true);
            return "auth/reset-password";
        }
        if (!result.success()) {
            model.addAttribute("token", form.getToken());
            model.addAttribute("error", result.errorMessage());
            return "auth/reset-password";
        }

        model.addAttribute("done", true);
        return "auth/reset-password";
    }
}
