# Workflow đặt lại mật khẩu

Tài liệu mô tả luồng từ lúc người dùng nhấn link trong email, nhập mật khẩu mới trên frontend, đến khi hệ thống cập nhật password hash và vô hiệu hóa token.

## 1. Tổng quan

```text
Người dùng nhấn link email
    ↓
GET /reset-password?token=...
    ↓ ChangePasswordController.resetPasswordPage()
ChangePasswordService.isValidResetToken()
    ├── thiếu/không tồn tại/revoked/used/hết hạn
    │       → hiển thị “Link không hợp lệ”
    └── hợp lệ
            → render form và giữ token trong hidden input
                    ↓ người dùng nhập newPassword + confirmPassword
POST /reset-password + token + CSRF
    ↓ @Valid ResetPasswordForm
    ├── lỗi → trả lại form
    └── hợp lệ → ChangePasswordService.resetPassword()
                        ↓ kiểm tra lại token
                Kiểm tra mật khẩu mới khác mật khẩu cũ
                        ↓ BCrypt encode
                Cập nhật User.passwordHash + passwordChangedAt
                        ↓
                Đánh dấu token usedAt
                        ↓
                Revoke SESSION và PASSWORD_RESET tokens
                        ↓ transaction commit
                Hiển thị đặt lại thành công
```

## 2. Mở link từ email

Link được tạo theo dạng:

```text
<app.base-url>/reset-password?token=<tokenValue>
```

`/reset-password` được `permitAll` tại [SecurityConfig.java (line 23)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L23).

GET đi vào [ChangePasswordController.resetPasswordPage() (line 21)](../src/main/java/com/edu/espp/controller/auth/ChangePasswordController.java#L21).

## 3. Kiểm tra token khi mở trang

Controller gọi [ChangePasswordService.isValidResetToken() (line 34)](../src/main/java/com/edu/espp/service/auth/ChangePasswordService.java#L34).

Service:

1. Từ chối token null/rỗng tại [line 36](../src/main/java/com/edu/espp/service/auth/ChangePasswordService.java#L36).
2. Tìm token bằng `tokenValue` và loại `PASSWORD_RESET` tại [line 40](../src/main/java/com/edu/espp/service/auth/ChangePasswordService.java#L40).
3. Gọi [isTokenActive() (line 153)](../src/main/java/com/edu/espp/service/auth/ChangePasswordService.java#L153).

Token chỉ active khi:

```text
revokedAt == null
usedAt == null
expiresAt != null
expiresAt > thời gian UTC hiện tại
```

Nếu không hợp lệ, controller đặt `invalidToken=true` tại [ChangePasswordController.java (line 23)](../src/main/java/com/edu/espp/controller/auth/ChangePasswordController.java#L23) và template hiển thị trạng thái lỗi tại [auth/reset-password.html (line 27)](../src/main/resources/templates/auth/reset-password.html#L27).

Nếu hợp lệ, controller:

```java
ResetPasswordForm form = new ResetPasswordForm();
form.setToken(token);
model.addAttribute("resetPasswordForm", form);
```

tại [ChangePasswordController.java (line 28)](../src/main/java/com/edu/espp/controller/auth/ChangePasswordController.java#L28), rồi render `auth/reset-password`.

## 4. Form frontend

Form nằm tại [auth/reset-password.html (line 62)](../src/main/resources/templates/auth/reset-password.html#L62):

```html
<form th:action="@{/reset-password}"
      method="post"
      th:object="${resetPasswordForm}">
```

- CSRF token: [line 64](../src/main/resources/templates/auth/reset-password.html#L64).
- Hidden reset token: [line 65](../src/main/resources/templates/auth/reset-password.html#L65).
- Mật khẩu mới: [line 70](../src/main/resources/templates/auth/reset-password.html#L70).
- Xác nhận mật khẩu: [line 98](../src/main/resources/templates/auth/reset-password.html#L98).
- Nút đặt lại: [line 124](../src/main/resources/templates/auth/reset-password.html#L124).

Trình duyệt gửi:

```http
POST /reset-password
Content-Type: application/x-www-form-urlencoded
```

```text
token=<reset token>
newPassword=<mật khẩu mới>
confirmPassword=<xác nhận mật khẩu>
_csrf=<csrf token>
```

## 5. Validate ResetPasswordForm

[ChangePasswordController.resetPassword() (line 36)](../src/main/java/com/edu/espp/controller/auth/ChangePasswordController.java#L36) nhận `@Valid ResetPasswordForm` và `BindingResult`.

[ResetPasswordForm.java](../src/main/java/com/edu/espp/dto/ResetPasswordForm.java) có:

- `token`: `@NotBlank` tại [line 18](../src/main/java/com/edu/espp/dto/ResetPasswordForm.java#L18).
- `newPassword`: `@NotBlank` + `@PasswordStrength` tại [line 21](../src/main/java/com/edu/espp/dto/ResetPasswordForm.java#L21).
- `confirmPassword`: `@NotBlank` tại [line 25](../src/main/java/com/edu/espp/dto/ResetPasswordForm.java#L25).
- `@FieldMatch` so sánh hai mật khẩu tại [line 15](../src/main/java/com/edu/espp/dto/ResetPasswordForm.java#L15).

Nếu có lỗi, controller giữ token trong model và trả lại form tại [ChangePasswordController.java (line 39)](../src/main/java/com/edu/espp/controller/auth/ChangePasswordController.java#L39).

Lưu ý: comment đầu `ResetPasswordForm` nói password được kiểm tra trong controller, nhưng code hiện tại thực tế dùng `@PasswordStrength` trong DTO.

## 6. Service xử lý reset

Controller gọi [ChangePasswordService.resetPassword() (line 52)](../src/main/java/com/edu/espp/service/auth/ChangePasswordService.java#L52). Method có `@Transactional`.

Service gọi [changePassword() (line 75)](../src/main/java/com/edu/espp/service/auth/ChangePasswordService.java#L75), và hàm này gọi [findValidToken() (line 126)](../src/main/java/com/edu/espp/service/auth/ChangePasswordService.java#L126) để kiểm tra token lại ngay lúc submit.

Việc kiểm tra lại ngăn dùng token đã hết hạn/revoke/used trong khoảng thời gian người dùng đang mở form.

## 7. Không cho dùng lại mật khẩu cũ

[ChangePasswordService.java (line 89)](../src/main/java/com/edu/espp/service/auth/ChangePasswordService.java#L89) lấy password hash hiện tại và gọi:

```java
passwordEncoder.matches(newPassword, currentPasswordHash)
```

tại [line 94](../src/main/java/com/edu/espp/service/auth/ChangePasswordService.java#L94).

Nếu giống mật khẩu cũ, service ném [SamePasswordException](../src/main/java/com/edu/espp/common/exception/SamePasswordException.java). `resetPassword()` chuyển lỗi này thành `ResetPasswordResult.error` tại [line 68](../src/main/java/com/edu/espp/service/auth/ChangePasswordService.java#L68).

Controller đưa message vào model `error` tại [ChangePasswordController.java (line 50)](../src/main/java/com/edu/espp/controller/auth/ChangePasswordController.java#L50).

## 8. Cập nhật mật khẩu

Nếu mật khẩu mới khác mật khẩu cũ:

```java
String encodedPassword = passwordEncoder.encode(newPassword);
user.setPasswordHash(encodedPassword);
user.setPasswordChangedAt(now);
userRepository.save(user);
```

Logic nằm tại [ChangePasswordService.java (line 103)](../src/main/java/com/edu/espp/service/auth/ChangePasswordService.java#L103).

Password được mã hóa bằng BCrypt từ [PasswordEncoderConfig.passwordEncoder() (line 17)](../src/main/java/com/edu/espp/config/PasswordEncoderConfig.java#L17).

## 9. Đánh dấu và revoke token

Token vừa dùng được đặt `usedAt = now` và lưu tại [ChangePasswordService.java (line 110)](../src/main/java/com/edu/espp/service/auth/ChangePasswordService.java#L110).

Sau đó service gọi [revokeActiveTokens() (line 169)](../src/main/java/com/edu/espp/service/auth/ChangePasswordService.java#L169) cho:

- `AuthTokenType.SESSION` tại [line 113](../src/main/java/com/edu/espp/service/auth/ChangePasswordService.java#L113).
- `AuthTokenType.PASSWORD_RESET` tại [line 118](../src/main/java/com/edu/espp/service/auth/ChangePasswordService.java#L118).

Các token được đặt `revokedAt = now` và lưu bằng `saveAll()`.

### Giới hạn của việc revoke SESSION hiện tại

Luồng login của project dùng Spring Security HTTP session/JSESSIONID và chưa thấy tạo bản ghi `AuthTokenType.SESSION`. Vì vậy revoke token SESSION trong database không trực tiếp invalidate các JSESSIONID đang hoạt động. Muốn bắt mọi thiết bị đăng nhập lại cần thêm cơ chế quản lý session/token tương ứng.

## 10. Kết quả trả về controller

[ChangePasswordService.resetPassword()](../src/main/java/com/edu/espp/service/auth/ChangePasswordService.java#L52) trả một trong ba trạng thái:

- Thành công: `ResetPasswordResult.ok()`.
- Token sai/hết hạn: `invalidToken`.
- Mật khẩu trùng mật khẩu cũ: `error`.

[ChangePasswordController.java (line 44)](../src/main/java/com/edu/espp/controller/auth/ChangePasswordController.java#L44) xử lý kết quả:

- Invalid token → `invalidToken=true`.
- Lỗi nghiệp vụ → giữ token và đặt `error`.
- Thành công → `done=true` tại [line 56](../src/main/java/com/edu/espp/controller/auth/ChangePasswordController.java#L56).

Template có ba trạng thái:

- Link sai/hết hạn: [auth/reset-password.html (line 27)](../src/main/resources/templates/auth/reset-password.html#L27).
- Thành công: [line 39](../src/main/resources/templates/auth/reset-password.html#L39).
- Form nhập mật khẩu: [line 52](../src/main/resources/templates/auth/reset-password.html#L52).

Sau thành công, hệ thống không redirect tự động. Người dùng nhấn “Đăng nhập ngay” tại [line 49](../src/main/resources/templates/auth/reset-password.html#L49).

## 11. Database

[AuthToken.java](../src/main/java/com/edu/espp/entity/AuthToken.java) lưu:

```text
token_type = PASSWORD_RESET
token_value
expires_at
revoked_at
used_at
created_at
user_id
```

[User.java](../src/main/java/com/edu/espp/entity/User.java) được cập nhật:

```text
password_hash = BCrypt hash mới
password_changed_at = thời gian UTC hiện tại
updated_at = cập nhật bởi @PreUpdate
```

## 12. File chính

- [auth/reset-password.html](../src/main/resources/templates/auth/reset-password.html)
- [ChangePasswordController.java](../src/main/java/com/edu/espp/controller/auth/ChangePasswordController.java)
- [ResetPasswordForm.java](../src/main/java/com/edu/espp/dto/ResetPasswordForm.java)
- [ChangePasswordService.java](../src/main/java/com/edu/espp/service/auth/ChangePasswordService.java)
- [AuthTokenRepository.java](../src/main/java/com/edu/espp/repository/auth/AuthTokenRepository.java)
- [AuthToken.java](../src/main/java/com/edu/espp/entity/AuthToken.java)
- [UserRepository.java](../src/main/java/com/edu/espp/repository/UserRepository.java)
- [User.java](../src/main/java/com/edu/espp/entity/User.java)
- [PasswordEncoderConfig.java](../src/main/java/com/edu/espp/config/PasswordEncoderConfig.java)
- [SecurityConfig.java](../src/main/java/com/edu/espp/config/SecurityConfig.java)

## 13. Tóm tắt

```text
Link email + token
    ↓ GET /reset-password
Kiểm tra token active
    ↓ form nhập password mới
POST /reset-password
    ↓ validate DTO
Kiểm tra lại token
    ↓ password mới khác password cũ
BCrypt encode + save User
    ↓
usedAt token + revoke token liên quan
    ↓ commit
Hiển thị thành công → link /login
```
