package com.edu.espp.controller;

import com.edu.espp.service.VerifyEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class VerifyEmailController {

    private final VerifyEmailService verifyEmailService;

    @GetMapping("/verify-email-sent")
    public String verifyEmailSentPage() {
        return "verify-email-sent";
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token, Model model) {
        VerifyEmailService.VerifyEmailResult result = verifyEmailService.verifyEmailForPage(token);

        if (!result.success()) {
            model.addAttribute("tokenError", result.errorMessage());
            return "verify-email";
        }

        return "redirect:/login?verified=true";
    }

    @PostMapping("/verify-email/resend")
    public String resendVerification(@RequestParam String email, Model model,
            RedirectAttributes redirectAttributes) {
        VerifyEmailService.ResendResult result = verifyEmailService.resendVerificationEmail(email);

        if (result.rateLimited()) {
            model.addAttribute("email", email);
            model.addAttribute("rateLimitError",
                    "Qua nhieu yeu cau. Thu lai sau " + result.retryAfterMinutes() + " phut");
            return "verify-email-sent";
        }

        redirectAttributes.addFlashAttribute("email", email);
        return "redirect:/verify-email-sent";
    }
}
