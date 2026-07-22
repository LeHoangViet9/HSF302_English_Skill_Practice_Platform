# Workflow đăng ký tài khoản

Tài liệu này được đồng bộ theo luồng Register hiện tại: từ lúc người dùng nhập tài khoản trên frontend đến khi hệ thống tạo `User`, `StudentUser` và chuyển về trang đăng nhập. Tên file và hàm đều có link tới đúng mã nguồn.

## 1. Tổng quan

```text
GET /register
    ↓ RegisterController.showRegisterPage()
Render form đăng ký
    ↓ người dùng nhập fullName, email, password, confirmPassword
POST /register + CSRF token
    ↓ Spring MVC bind vào RegisterForm
Jakarta Validation + checkConfirmPassword()
    ├── fullName: bắt buộc, 2–150 ký tự
    ├── email: hợp lệ, tối đa 255, không trùng
    ├── password: ≥ 8 ký tự, có chữ hoa và số
    └── confirmPassword: phải khớp password
    ├── Có lỗi → trả lại auth/register
    └── Hợp lệ → RegisterService.register() @Transactional
    ↓ chuẩn hóa email và họ tên
Kiểm tra email trùng lần nữa
    ↓ BCrypt mã hóa password
User(STUDENT, ACTIVE) → userRepository.save()
    ↓
StudentUser(user) → studentUserRepository.save()
    ↓ commit transaction
Redirect /login?registered
    ↓
Hiển thị thông báo đăng ký thành công
```

## 2. Mở trang đăng ký

`/register` được cho phép truy cập công khai tại [SecurityConfig.java (line 21)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L21).

Khi trình duyệt gửi:

```http
GET /register
```

request đi vào [RegisterController.showRegisterPage() (line 23)](../src/main/java/com/edu/espp/controller/auth/RegisterController.java#L23):

```java
@GetMapping("/register")
public String showRegisterPage(Model model) {
    model.addAttribute("registerForm", new RegisterForm());
    return "auth/register";
}
```

Controller tạo [RegisterForm](../src/main/java/com/edu/espp/dto/RegisterForm.java) rỗng và đưa vào model để Thymeleaf binding dữ liệu.

### Đường dẫn view

File giao diện nằm tại [auth/register.html](../src/main/resources/templates/auth/register.html). Controller hiện trả đúng view tương ứng tại [line 30](../src/main/java/com/edu/espp/controller/auth/RegisterController.java#L30):

```java
return "auth/register";
```

Cả ba nhánh render form đều dùng view này:

- Mở form bằng `GET /register`: [line 30](../src/main/java/com/edu/espp/controller/auth/RegisterController.java#L30).
- Dữ liệu validation không hợp lệ: [line 41](../src/main/java/com/edu/espp/controller/auth/RegisterController.java#L41).
- Service phát sinh exception: [line 52](../src/main/java/com/edu/espp/controller/auth/RegisterController.java#L52).

## 3. Người dùng nhập dữ liệu trên frontend

Form nằm tại [auth/register.html (line 31)](../src/main/resources/templates/auth/register.html#L31):

```html
<form id="registerForm"
      th:action="@{/register}"
      method="post"
      th:object="${registerForm}">
```

### CSRF token

Token nằm tại [line 33](../src/main/resources/templates/auth/register.html#L33):

```html
<input type="hidden"
       th:name="${_csrf.parameterName}"
       th:value="${_csrf.token}">
```

Request POST thiếu token hợp lệ sẽ bị Spring Security chặn trước controller.

### Các input

- Họ tên: [`fullName` (line 37)](../src/main/resources/templates/auth/register.html#L37)
- Email: [`email` (line 46)](../src/main/resources/templates/auth/register.html#L46)
- Mật khẩu: [`password` (line 56)](../src/main/resources/templates/auth/register.html#L56)
- Xác nhận mật khẩu: [`confirmPassword` (line 86)](../src/main/resources/templates/auth/register.html#L86)
- Nút tạo tài khoản: [line 112](../src/main/resources/templates/auth/register.html#L112)

Khi nhấn nút, trình duyệt gửi:

```http
POST /register
Content-Type: application/x-www-form-urlencoded
```

```text
fullName=<họ tên>
email=<email>
password=<mật khẩu>
confirmPassword=<mật khẩu xác nhận>
_csrf=<csrf token>
```

## 4. Controller nhận và bind dữ liệu

Request đi vào [RegisterController.register() (line 33)](../src/main/java/com/edu/espp/controller/auth/RegisterController.java#L33):

```java
@PostMapping("/register")
public String register(
        @Valid @ModelAttribute("registerForm") RegisterForm form,
        BindingResult bindingResult
) {
    // validate và gọi service
}
```

- `@ModelAttribute` bind request vào `RegisterForm`.
- `@Valid` kích hoạt Jakarta Validation.
- `BindingResult` chứa lỗi để hiển thị lại trên frontend.

## 5. Validate RegisterForm

DTO là [RegisterForm.java](../src/main/java/com/edu/espp/dto/RegisterForm.java).

### Họ tên

[`RegisterForm.fullName` (line 18)](../src/main/java/com/edu/espp/dto/RegisterForm.java#L18) dùng `@NotBlank` và `@Size(min = 2, max = 150)`.

### Email

[`RegisterForm.email` (line 24)](../src/main/java/com/edu/espp/dto/RegisterForm.java#L24) dùng:

```java
@NotBlank
@Email
@Size(max = 255)
@UniqueEmail
private String email;
```

[UniqueEmailValidator.isValid() (line 19)](../src/main/java/com/edu/espp/common/validation/UniqueEmailValidator.java#L19) trim, chuyển email về chữ thường rồi gọi:

```java
return !userRepository.existsByEmail(email);
```

Method database nằm tại [UserRepository.existsByEmail() (line 13)](../src/main/java/com/edu/espp/repository/UserRepository.java#L13).

### Độ mạnh mật khẩu

[`RegisterForm.password` (line 30)](../src/main/java/com/edu/espp/dto/RegisterForm.java#L30) dùng `@PasswordStrength`.

[PasswordStrengthValidator.isValid() (line 9)](../src/main/java/com/edu/espp/common/validation/PasswordStrengthValidator.java#L9) kiểm tra:

```java
boolean hasMinLength = value.length() >= 8;
boolean hasUppercase = value.chars().anyMatch(Character::isUpperCase);
boolean hasDigit = value.chars().anyMatch(Character::isDigit);
```

### Mật khẩu xác nhận

`RegisterForm` dùng [`@FieldMatch` (line 16)](../src/main/java/com/edu/espp/dto/RegisterForm.java#L16). Logic so sánh nằm tại [FieldMatchValidator.isValid() (line 23)](../src/main/java/com/edu/espp/common/validation/FieldMatchValidator.java#L23). Nếu không khớp, lỗi được gắn vào `confirmPassword` tại [line 33](../src/main/java/com/edu/espp/common/validation/FieldMatchValidator.java#L33).

Sau khi `@Valid` chạy, controller tiếp tục gọi [checkConfirmPassword() (line 58)](../src/main/java/com/edu/espp/controller/auth/RegisterController.java#L58). Vì `RegisterForm` cũng có `@FieldMatch`, luồng hiện tại kiểm tra `password == confirmPassword` hai lần trước khi đọc `BindingResult`.

### Trả lỗi về frontend

[RegisterController.java (line 40)](../src/main/java/com/edu/espp/controller/auth/RegisterController.java#L40) kiểm tra:

```java
if (bindingResult.hasErrors()) {
    return "auth/register";
}
```

Các thẻ `th:errors` trong form hiển thị lỗi cạnh field tương ứng.

## 6. RegisterService xử lý nghiệp vụ

Khi dữ liệu hợp lệ, controller gọi [RegisterService.register() (line 26)](../src/main/java/com/edu/espp/service/auth/RegisterService.java#L26). Method có `@Transactional` nên việc lưu hai bảng nằm trong cùng transaction.

### Chuẩn hóa dữ liệu

[RegisterService.java (line 29)](../src/main/java/com/edu/espp/service/auth/RegisterService.java#L29):

```java
String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
String fullName = form.getFullName().trim();
```

### Kiểm tra email trùng lần nữa

[RegisterService.java (line 35)](../src/main/java/com/edu/espp/service/auth/RegisterService.java#L35):

```java
if (userRepository.existsByEmail(email)) {
    throw new EmailExistsException();
}
```

Kiểm tra lại ở service giúp giảm rủi ro hai request đăng ký cùng email. Exception nằm tại [EmailExistsException.java](../src/main/java/com/edu/espp/common/exception/EmailExistsException.java).

## 7. Mã hóa mật khẩu và tạo User

[RegisterService.java (line 39)](../src/main/java/com/edu/espp/service/auth/RegisterService.java#L39) tạo User:

```java
User user = User.builder()
        .email(email)
        .fullName(fullName)
        .passwordHash(passwordEncoder.encode(form.getPassword()))
        .role(Role.STUDENT)
        .status(UserStatus.ACTIVE)
        .loginAttempts(0)
        .build();
```

`PasswordEncoder` là BCrypt tại [PasswordEncoderConfig.passwordEncoder() (line 17)](../src/main/java/com/edu/espp/config/PasswordEncoderConfig.java#L17). Mật khẩu thô và `confirmPassword` không được lưu.

[RegisterService.java (line 50)](../src/main/java/com/edu/espp/service/auth/RegisterService.java#L50) lưu:

```java
userRepository.save(user);
```

[User.onCreate() (line 64)](../src/main/java/com/edu/espp/entity/User.java#L64) tự gán `createdAt` và `updatedAt`.

Tài khoản mới luôn có:

```text
role = STUDENT
status = ACTIVE
loginAttempts = 0
```

## 8. Tạo StudentUser

[RegisterService.java (line 52)](../src/main/java/com/edu/espp/service/auth/RegisterService.java#L52) tạo profile:

```java
StudentUser student = StudentUser.builder()
        .user(user)
        .build();
```

[StudentUser.user (line 25)](../src/main/java/com/edu/espp/entity/StudentUser.java#L25) là quan hệ `@OneToOne` tới User.

[RegisterService.java (line 56)](../src/main/java/com/edu/espp/service/auth/RegisterService.java#L56) gọi:

```java
studentUserRepository.save(student);
```

Repository nằm tại [StudentUserRepository.java](../src/main/java/com/edu/espp/repository/StudentUserRepository.java). [StudentUser.onCreate() (line 39)](../src/main/java/com/edu/espp/entity/StudentUser.java#L39) tự gán thời gian tạo/cập nhật.

Vì method là transaction:

- Lưu cả User và StudentUser thành công → commit.
- Lưu User xong nhưng StudentUser lỗi → rollback cả User.

## 9. Xử lý exception

[RegisterController.java (line 44)](../src/main/java/com/edu/espp/controller/auth/RegisterController.java#L44) hiện dùng:

```java
try {
    registerService.register(form);
} catch (Exception exception) {
    bindingResult.rejectValue(
            "email",
            "email.exists",
            "Email này đã được sử dụng"
    );
    return "auth/register";
}
```

Theo code hiện tại, `catch (Exception)` bắt mọi exception và gắn chúng vào field email với thông báo “Email này đã được sử dụng”. Vì vậy lỗi database, BCrypt hoặc StudentUser cũng đi qua cùng nhánh hiển thị này.

Nếu sau này muốn phân biệt đúng loại lỗi, có thể thu hẹp thành:

```java
catch (EmailExistsException exception) {
    // gắn lỗi email đã tồn tại
}
```

Các lỗi khác nên được ghi log hoặc để global exception handler xử lý.

## 10. Đăng ký thành công

[RegisterController.java (line 55)](../src/main/java/com/edu/espp/controller/auth/RegisterController.java#L55) trả:

```java
return "redirect:/login?registered";
```

Trình duyệt gửi `GET /login?registered`. [LoginController.showLoginPage() (line 16)](../src/main/java/com/edu/espp/controller/auth/LoginController.java#L16) nhận request và [line 32](../src/main/java/com/edu/espp/controller/auth/LoginController.java#L32) tạo thông báo đăng ký thành công.

Đăng ký không tự đăng nhập và không tạo Authentication/session. Người dùng phải đăng nhập bằng tài khoản vừa tạo.

## 11. Dữ liệu được tạo trong database

```text
users
├── email đã chuẩn hóa
├── password_hash BCrypt
├── full_name đã trim
├── role = STUDENT
├── status = ACTIVE
├── login_attempts = 0
├── created_at
└── updated_at

student_users
├── student_id
├── user_id → users.id
├── created_at
└── updated_at
```

## 12. Các file chính

### Frontend

- [auth/register.html](../src/main/resources/templates/auth/register.html)
- [auth/login.html](../src/main/resources/templates/auth/login.html)

### Controller và DTO

- [RegisterController.java](../src/main/java/com/edu/espp/controller/auth/RegisterController.java)
- [RegisterForm.java](../src/main/java/com/edu/espp/dto/RegisterForm.java)
- [LoginController.java](../src/main/java/com/edu/espp/controller/auth/LoginController.java)

### Validation

- [UniqueEmail.java](../src/main/java/com/edu/espp/common/validation/UniqueEmail.java)
- [UniqueEmailValidator.java](../src/main/java/com/edu/espp/common/validation/UniqueEmailValidator.java)
- [PasswordStrength.java](../src/main/java/com/edu/espp/common/validation/PasswordStrength.java)
- [PasswordStrengthValidator.java](../src/main/java/com/edu/espp/common/validation/PasswordStrengthValidator.java)
- [FieldMatch.java](../src/main/java/com/edu/espp/common/validation/FieldMatch.java)
- [FieldMatchValidator.java](../src/main/java/com/edu/espp/common/validation/FieldMatchValidator.java)

### Service, Repository và Entity

- [RegisterService.java](../src/main/java/com/edu/espp/service/auth/RegisterService.java)
- [UserRepository.java](../src/main/java/com/edu/espp/repository/UserRepository.java)
- [StudentUserRepository.java](../src/main/java/com/edu/espp/repository/StudentUserRepository.java)
- [User.java](../src/main/java/com/edu/espp/entity/User.java)
- [StudentUser.java](../src/main/java/com/edu/espp/entity/StudentUser.java)
- [PasswordEncoderConfig.java](../src/main/java/com/edu/espp/config/PasswordEncoderConfig.java)
- [EmailExistsException.java](../src/main/java/com/edu/espp/common/exception/EmailExistsException.java)
- [SecurityConfig.java](../src/main/java/com/edu/espp/config/SecurityConfig.java)

## 13. Tóm tắt

```text
GET /register
    ↓ RegisterController.showRegisterPage()
auth/register.html
    ↓ POST /register + CSRF
RegisterController.register()
    ↓ RegisterForm + @Valid
UniqueEmail + PasswordStrength + FieldMatch
    ↓ hợp lệ
RegisterService.register() @Transactional
    ↓
BCryptPasswordEncoder.encode(password)
    ↓
UserRepository.save(User STUDENT, ACTIVE)
    ↓
StudentUserRepository.save(StudentUser)
    ↓ commit
Redirect /login?registered
```
