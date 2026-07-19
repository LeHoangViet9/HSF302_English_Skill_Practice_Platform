package com.edu.espp.controller.auth;

import com.edu.espp.dto.RegisterForm;
import com.edu.espp.service.auth.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    @GetMapping("/register")
    public String showRegisterPage(Model model) {

        model.addAttribute(
                "registerForm",
                new RegisterForm());

        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerForm") RegisterForm form,
            BindingResult bindingResult) {

        checkConfirmPassword(form, bindingResult);

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            registerService.register(form);
        } catch (Exception exception) {
            bindingResult.rejectValue(
                    "email",
                    "email.exists",
                    "Email này đã được sử dụng");

            return "register";
        }

        return "redirect:/login?registered";
    }

    private void checkConfirmPassword(
            RegisterForm form,
            BindingResult bindingResult) {

        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue(
                    "confirmPassword",
                    "password.mismatch",
                    "Mật khẩu xác nhận không khớp");
        }
    }
}
