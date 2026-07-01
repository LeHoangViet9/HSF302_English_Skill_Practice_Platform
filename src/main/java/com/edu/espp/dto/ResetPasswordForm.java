package com.edu.espp.dto;

import com.edu.espp.common.validation.FieldMatch;
import com.edu.espp.common.validation.PasswordStrength;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Form-backing object for POST /reset-password. Password strength rules are
 * checked field-by-field in the controller (not via {@code @PasswordStrength})
 * so each broken rule can surface its own message, per
 * .sdd/Spect/Backend/feat-auth/UC-03-reset-password.md §5.
 */
@Data
@FieldMatch(first = "newPassword", second = "confirmPassword", message = "Mat khau xac nhan khong khop")
public class ResetPasswordForm {

    @NotBlank(message = "Link không hợp lệ")
    private String token;

    @NotBlank(message = "Vui lòng nhập mật khẩu mới")
    @PasswordStrength
    private String newPassword;

    @NotBlank(message = "Vui lòng xác nhận mật khẩu")
    private String confirmPassword;
}
