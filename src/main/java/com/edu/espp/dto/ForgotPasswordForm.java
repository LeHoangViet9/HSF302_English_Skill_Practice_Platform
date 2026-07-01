package com.edu.espp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Form-backing object for POST /forgot-password.
 * See .sdd/Spect/Backend/feat-auth/UC-03-reset-password.md §5.
 */
@Data
public class ForgotPasswordForm {

    @NotBlank(message = "Email là bắt buộc")
    @Email(message = "Email không hợp lệ")
    private String email;
}
