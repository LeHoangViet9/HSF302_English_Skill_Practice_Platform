# Workflow quên mật khẩu

Tài liệu mô tả luồng từ lúc người dùng nhập email trên frontend đến khi hệ thống tạo token, lưu database và gửi link đặt lại mật khẩu.

## 1. Tổng quan

```text
GET /forgot-password
    ↓ ForgotPasswordController.forgotPasswordPage()
Render auth/forgot-password.html
    ↓ người dùng nhập email
POST /forgot-password + CSRF
    ↓ @Valid ForgotPasswordForm
    ├── Sai định dạng → trả lại form
    └── Hợp lệ → ForgotPasswordService.requestPasswordReset()
                        ↓ chuẩn hóa email
                UserRepository.findByEmail()
                        ↓
                Email không tồn tại/User không ACTIVE/không có password
                    → trả kết quả OK để không lộ tài khoản
                        ↓ nếu User hợp lệ
                Kiểm tra rate limit: tối đa 3 token/30 giây
                        ↓
                Revoke token PASSWORD_RESET cũ
                        ↓
                Tạo token ngẫu nhiên 32 byte, hạn 15 phút
                        ↓
                AuthTokenRepository.save()
                        ↓
                EmailService.sendResetPasswordEmail() @Async
    ↓
Redirect /forgot-password?sent=true
    ↓
Hiển thị “Nếu email tồn tại...”
```

## 2. Mở form

`/forgot-password` được `permitAll` tại [SecurityConfig.java (line 22)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L22).

`GET /forgot-password` đi vào [ForgotPasswordController.forgotPasswordPage() (line 21)](../src/main/java/com/edu/espp/controller/auth/ForgotPasswordController.java#L21).

Controller tạo [ForgotPasswordForm](../src/main/java/com/edu/espp/dto/ForgotPasswordForm.java), xử lý trạng thái `sent=true` và trả [auth/forgot-password.html](../src/main/resources/templates/auth/forgot-password.html).

## 3. Người dùng nhập email

Form nằm tại [auth/forgot-password.html (line 50)](../src/main/resources/templates/auth/forgot-password.html#L50):

```html
<form th:action="@{/forgot-password}"
      method="post"
      th:object="${forgotPasswordForm}">
```

- CSRF token: [line 52](../src/main/resources/templates/auth/forgot-password.html#L52).
- Input email: [line 56](../src/main/resources/templates/auth/forgot-password.html#L56).
- Nút gửi link: [line 64](../src/main/resources/templates/auth/forgot-password.html#L64).

Trình duyệt gửi:

```http
POST /forgot-password
Content-Type: application/x-www-form-urlencoded
```

```text
email=<email người dùng nhập>
_csrf=<csrf token>
```

## 4. Validate frontend data

[ForgotPasswordController.forgotPassword() (line 32)](../src/main/java/com/edu/espp/controller/auth/ForgotPasswordController.java#L32) nhận:

```java
@Valid @ModelAttribute("forgotPasswordForm") ForgotPasswordForm form,
BindingResult bindingResult
```

[ForgotPasswordForm.email (line 14)](../src/main/java/com/edu/espp/dto/ForgotPasswordForm.java#L14) dùng:

```java
@NotBlank
@Email
private String email;
```

Nếu có lỗi, controller trả lại `auth/forgot-password` tại [line 35](../src/main/java/com/edu/espp/controller/auth/ForgotPasswordController.java#L35).

## 5. Service tìm tài khoản

Controller gọi [ForgotPasswordService.requestPasswordReset() (line 41)](../src/main/java/com/edu/espp/service/auth/ForgotPasswordService.java#L41).

[ForgotPasswordService.normalizeEmail() (line 161)](../src/main/java/com/edu/espp/service/auth/ForgotPasswordService.java#L161) trim và chuyển email về chữ thường. Sau đó service gọi `userRepository.findByEmail()` tại [line 45](../src/main/java/com/edu/espp/service/auth/ForgotPasswordService.java#L45).

Nếu email không tồn tại, service trả `ok()` tại [line 48](../src/main/java/com/edu/espp/service/auth/ForgotPasswordService.java#L48), không báo “email không tồn tại”. Đây là cơ chế chống dò tài khoản.

Service cũng trả `ok()` nếu User không có password hash hoặc `status != ACTIVE` tại [line 54](../src/main/java/com/edu/espp/service/auth/ForgotPasswordService.java#L54).

## 6. Rate limit

Các giới hạn nằm tại [ForgotPasswordService.java (line 29)](../src/main/java/com/edu/espp/service/auth/ForgotPasswordService.java#L29):

```java
PASSWORD_RESET_VALIDITY_MINUTES = 15;
PASSWORD_RESET_RATE_LIMIT_MAX = 3;
PASSWORD_RESET_RATE_LIMIT_WINDOW_SECONDS = 30;
```

[checkResetRequestRateLimit() (line 80)](../src/main/java/com/edu/espp/service/auth/ForgotPasswordService.java#L80) lấy các token được tạo trong 30 giây gần nhất bằng [AuthTokenRepository.findByUser_IdAndTokenTypeAndCreatedAtAfterOrderByCreatedAtAsc() (line 28)](../src/main/java/com/edu/espp/repository/auth/AuthTokenRepository.java#L28).

Nếu đã có ít nhất 3 token, service trả `rateLimited` và số giây cần chờ.

### Sai đơn vị ở thông báo hiện tại

[ForgotPasswordController.java (line 42)](../src/main/java/com/edu/espp/controller/auth/ForgotPasswordController.java#L42) đang nối `retryAfterSeconds()` nhưng ghi đơn vị “phút”:

```java
"Quá nhiều yêu cầu vui lòng đăng nhập lại sau"
    + result.retryAfterSeconds()
    + " phút."
```

Giá trị thực tế là **giây**. Chuỗi cũng thiếu khoảng trắng trước con số. Nội dung đúng nên dùng “sau X giây”.

## 7. Revoke token cũ

[revokeActiveResetTokens() (line 120)](../src/main/java/com/edu/espp/service/auth/ForgotPasswordService.java#L120) lấy token `PASSWORD_RESET` chưa revoke:

```java
authTokenRepository
    .findByUser_IdAndTokenTypeAndRevokedAtIsNull(...)
```

Service đặt `revokedAt = now` rồi gọi `saveAll()` tại [line 132](../src/main/java/com/edu/espp/service/auth/ForgotPasswordService.java#L132). Vì vậy link reset cũ không còn hợp lệ khi link mới được tạo.

## 8. Tạo token mới

[sendResetPasswordToken() (line 135)](../src/main/java/com/edu/espp/service/auth/ForgotPasswordService.java#L135):

1. Gọi [generateSecureToken() (line 168)](../src/main/java/com/edu/espp/service/auth/ForgotPasswordService.java#L168).
2. Dùng `SecureRandom` tạo 32 byte.
3. Encode Base64 URL-safe, không padding.
4. Tạo [AuthToken](../src/main/java/com/edu/espp/entity/AuthToken.java) loại `PASSWORD_RESET`.
5. Đặt `expiresAt = now + 15 phút`.
6. Lưu qua [AuthTokenRepository.save() (line 150)](../src/main/java/com/edu/espp/service/auth/ForgotPasswordService.java#L150).

Link được ghép tại [line 152](../src/main/java/com/edu/espp/service/auth/ForgotPasswordService.java#L152):

```text
<app.base-url>/reset-password?token=<tokenValue>
```

Các trường token nằm trong [AuthToken.java (line 51)](../src/main/java/com/edu/espp/entity/AuthToken.java#L51): User, token type, token value, expiry, revoked time, used time và created time.

## 9. Gửi email

Service gọi [EmailService.sendResetPasswordEmail() (line 45)](../src/main/java/com/edu/espp/service/auth/EmailService.java#L45). Method có `@Async` nên gửi email ở thread khác.

[EmailService.java (line 55)](../src/main/java/com/edu/espp/service/auth/EmailService.java#L55) render template [templates/reset-password.html](../src/main/resources/templates/reset-password.html) với biến `resetLink`.

[EmailService.send() (line 62)](../src/main/java/com/edu/espp/service/auth/EmailService.java#L62) cấu hình SMTP STARTTLS và gọi `Transport.send()` tại [line 118](../src/main/java/com/edu/espp/service/auth/EmailService.java#L118).

Nếu username/password mail chưa cấu hình, method chỉ ghi log và return tại [line 67](../src/main/java/com/edu/espp/service/auth/EmailService.java#L67). Frontend vẫn hiển thị trạng thái đã gửi.

## 10. Kết quả frontend

Nếu không bị rate-limit, controller redirect tại [ForgotPasswordController.java (line 47)](../src/main/java/com/edu/espp/controller/auth/ForgotPasswordController.java#L47):

```java
return "redirect:/forgot-password?sent=true";
```

GET tiếp theo đặt `sent=true` và template hiển thị trạng thái thành công tại [auth/forgot-password.html (line 27)](../src/main/resources/templates/auth/forgot-password.html#L27):

> Nếu email tồn tại, bạn sẽ nhận được link đặt lại mật khẩu trong vài phút.

## 11. File chính

- [auth/forgot-password.html](../src/main/resources/templates/auth/forgot-password.html)
- [ForgotPasswordController.java](../src/main/java/com/edu/espp/controller/auth/ForgotPasswordController.java)
- [ForgotPasswordForm.java](../src/main/java/com/edu/espp/dto/ForgotPasswordForm.java)
- [ForgotPasswordService.java](../src/main/java/com/edu/espp/service/auth/ForgotPasswordService.java)
- [UserRepository.java](../src/main/java/com/edu/espp/repository/UserRepository.java)
- [AuthTokenRepository.java](../src/main/java/com/edu/espp/repository/auth/AuthTokenRepository.java)
- [AuthToken.java](../src/main/java/com/edu/espp/entity/AuthToken.java)
- [EmailService.java](../src/main/java/com/edu/espp/service/auth/EmailService.java)
- [Email template](../src/main/resources/templates/reset-password.html)
- [SecurityConfig.java](../src/main/java/com/edu/espp/config/SecurityConfig.java)

## 12. Tóm tắt

```text
Email frontend
    ↓ validate
UserRepository.findByEmail()
    ↓ chống dò tài khoản
Rate limit
    ↓ revoke link cũ
Tạo AuthToken 15 phút
    ↓ save DB
EmailService @Async
    ↓
Người dùng nhận /reset-password?token=...
```
