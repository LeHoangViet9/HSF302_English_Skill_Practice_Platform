package edu.fu.dto;

import edu.fu.common.validation.FieldMatch;
import edu.fu.common.validation.PasswordStrength;
import edu.fu.common.validation.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Form-backing object for POST /register.
 * See .sdd/Spect/Backend/feat-auth/UC-02-register.md §5.
 */
@Data
@FieldMatch(first = "password", second = "confirmPassword", message = "Mat khau xac nhan khong khop")
public class RegisterForm {

    @NotBlank(message = "Họ tên là bắt buộc")
    @Size(min = 2, message = "Họ tên phải có ít nhất 2 ký tự")
    @Size(max = 150, message = "Họ tên không được vượt quá 150 ký tự")
    private String fullName;

    @NotBlank(message = "Email là bắt buộc")
    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email quá dài (tối đa 255 ký tự)")
    @UniqueEmail
    private String email;

    @NotBlank(message = "Mật khẩu là bắt buộc")
    @PasswordStrength
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu là bắt buộc")
    private String confirmPassword;
}
