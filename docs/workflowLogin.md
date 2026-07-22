# Workflow đăng nhập

Tài liệu này mô tả toàn bộ quy trình đăng nhập từ lúc người dùng mở giao diện, nhập email/mật khẩu, gửi form, được Spring Security xác thực, tạo session và chuyển hướng theo quyền.

## 1. Tổng quan luồng

```text
Người dùng mở GET /login
        ↓
LoginController.showLoginPage()
        ↓
Render auth/login.html
        ↓
Người dùng nhập email + password
        ↓
Form gửi POST /login + CSRF token
        ↓
Spring Security UsernamePasswordAuthenticationFilter
        ↓
StudentDetailsService.loadUserByUsername(email)
        ↓
UserRepository.findByEmail(normalizedEmail)
        ↓
Đọc User từ database
        ↓
Kiểm tra status == ACTIVE
        ↓
BCryptPasswordEncoder đối chiếu mật khẩu
        ↓
Đúng: tạo Authentication + SecurityContext + HTTP session
Sai: redirect /login?error
        ↓
Đúng: successHandler kiểm tra role
        ├── ADMIN   → /admin/dashboard
        ├── STAFF   → /staff/dashboard
        └── STUDENT → /dashboard
```

## 2. Mở trang đăng nhập

Khi người dùng truy cập:

```http
GET /login
```

request đi vào [LoginController.showLoginPage() (line 16)](../src/main/java/com/edu/espp/controller/auth/LoginController.java#L16).

```java
@GetMapping("/login")
public String showLoginPage(
        @RequestParam(name = "error", required = false) String error,
        @RequestParam(name = "registered", required = false) String registered,
        @RequestParam(name = "logout", required = false) String logout,
        @RequestParam(name = "reset", required = false) String reset,
        Model model
) {
    // Chuẩn bị thông báo cho giao diện
    return "auth/login";
}
```

Controller trả về template [auth/login.html](../src/main/resources/templates/auth/login.html).

### Thông báo trên trang login

[LoginController.showLoginPage() (line 28)](../src/main/java/com/edu/espp/controller/auth/LoginController.java#L28) kiểm tra query parameter:

- `/login?error`: hiển thị email hoặc mật khẩu không đúng.
- `/login?registered`: thông báo đăng ký thành công.
- `/login?reset`: thông báo đặt lại mật khẩu thành công.
- `/login?logout`: thông báo đăng xuất thành công.

## 3. Người dùng nhập tài khoản trên frontend

Form nằm tại [auth/login.html (line 66)](../src/main/resources/templates/auth/login.html#L66):

```html
<form class="login-form"
      id="loginForm"
      th:action="@{/login}"
      method="post">
```

### 3.1. CSRF token

Form gửi kèm CSRF token tại [auth/login.html (line 68)](../src/main/resources/templates/auth/login.html#L68):

```html
<input type="hidden"
       th:name="${_csrf.parameterName}"
       th:value="${_csrf.token}">
```

Spring Security kiểm tra token này trước khi xử lý POST. Request thiếu hoặc sai CSRF token sẽ bị từ chối.

### 3.2. Trường email

Input email nằm tại [auth/login.html (line 76)](../src/main/resources/templates/auth/login.html#L76):

```html
<input id="email"
       name="email"
       type="email"
       required>
```

Tên field là `email`, phải khớp với `usernameParameter("email")` trong SecurityConfig.

### 3.3. Trường mật khẩu

Trang login gọi fragment tại [auth/login.html (line 83)](../src/main/resources/templates/auth/login.html#L83). Input thật nằm trong [password-input.html (line 10)](../src/main/resources/templates/auth/fragments/password-input.html#L10):

```html
<input type="password"
       th:id="${fieldId}"
       th:name="${fieldName}"
       required>
```

Khi fragment được gọi từ trang login, `fieldName` có giá trị `password`, nên request gửi lên có:

```text
email=<email người dùng nhập>
password=<mật khẩu người dùng nhập>
_csrf=<csrf token>
```

### 3.4. Submit form

Nút submit nằm tại [auth/login.html (line 93)](../src/main/resources/templates/auth/login.html#L93):

```html
<button class="btn-login" type="submit">
    Đăng nhập
</button>
```

Khi nhấn nút, trình duyệt gửi:

```http
POST /login
Content-Type: application/x-www-form-urlencoded
```

## 4. Ai xử lý POST /login?

`POST /login` **không đi vào LoginController**. Điều này cũng được ghi chú tại [LoginController.java (line 8)](../src/main/java/com/edu/espp/controller/auth/LoginController.java#L8).

Request được Spring Security tiếp nhận theo cấu hình [SecurityConfig.securityFilterChain() (line 12)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L12).

Phần form login bắt đầu tại [SecurityConfig.java (line 51)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L51):

```java
.formLogin(form -> form
        .loginPage("/login")
        .loginProcessingUrl("/login")
        .usernameParameter("email")
        .passwordParameter("password")
        // success/failure handling
)
```

Các cấu hình chính:

- [loginPage("/login") (line 54)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L54): URL hiển thị form.
- [loginProcessingUrl("/login") (line 57)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L57): URL nhận POST đăng nhập.
- [usernameParameter("email") (line 60)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L60): đọc email từ request.
- [passwordParameter("password") (line 61)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L61): đọc mật khẩu từ request.

Spring Security dùng `UsernamePasswordAuthenticationFilter` để đọc hai tham số và bắt đầu xác thực.

## 5. Tìm tài khoản trong database

Trong quá trình xác thực, Spring Security gọi [StudentDetailsService.loadUserByUsername() (line 23)](../src/main/java/com/edu/espp/service/auth/StudentDetailsService.java#L23).

### 5.1. Chuẩn hóa email

Email được bỏ khoảng trắng hai đầu và chuyển thành chữ thường tại [StudentDetailsService.java (line 26)](../src/main/java/com/edu/espp/service/auth/StudentDetailsService.java#L26):

```java
String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
```

Ví dụ:

```text
"  Student@ESPP.COM  " → "student@espp.com"
```

### 5.2. Truy vấn UserRepository

[StudentDetailsService.java (line 28)](../src/main/java/com/edu/espp/service/auth/StudentDetailsService.java#L28) gọi:

```java
userRepository.findByEmail(normalizedEmail)
```

Method repository được khai báo tại [UserRepository.findByEmail() (line 11)](../src/main/java/com/edu/espp/repository/UserRepository.java#L11):

```java
Optional<User> findByEmail(String email);
```

Spring Data JPA tự tạo câu truy vấn tương ứng để tìm bản ghi trong bảng `users`.

Nếu không có email, service ném `UsernameNotFoundException`. Spring Security coi đây là đăng nhập thất bại.

## 6. Kiểm tra trạng thái tài khoản

Sau khi tìm thấy User, [StudentDetailsService.java (line 33)](../src/main/java/com/edu/espp/service/auth/StudentDetailsService.java#L33) kiểm tra:

```java
if (user.getStatus() != UserStatus.ACTIVE) {
    throw new DisabledException("Tài khoản không hoạt động");
}
```

Trường trạng thái được khai báo tại [User.status (line 37)](../src/main/java/com/edu/espp/entity/User.java#L37).

Chỉ tài khoản có:

```text
status = ACTIVE
```

mới tiếp tục được xác thực.

## 7. Tạo UserDetails cho Spring Security

Nếu tài khoản tồn tại và đang hoạt động, [StudentDetailsService.java (line 38)](../src/main/java/com/edu/espp/service/auth/StudentDetailsService.java#L38) trả về:

```java
return org.springframework.security.core.userdetails.User
        .builder()
        .username(user.getEmail())
        .password(user.getPasswordHash())
        .roles(user.getRole().name())
        .build();
```

Dữ liệu được ánh xạ:

| Entity User | Spring Security UserDetails |
|---|---|
| `email` | `username` |
| `passwordHash` | `password` |
| `role` | authority `ROLE_<role>` |

Các trường liên quan nằm trong [User.java](../src/main/java/com/edu/espp/entity/User.java):

- [passwordHash (line 26)](../src/main/java/com/edu/espp/entity/User.java#L26)
- [role (line 32)](../src/main/java/com/edu/espp/entity/User.java#L32)
- [status (line 37)](../src/main/java/com/edu/espp/entity/User.java#L37)

## 8. Đối chiếu mật khẩu bằng BCrypt

Bean mã hóa mật khẩu được khai báo tại [PasswordEncoderConfig.passwordEncoder() (line 17)](../src/main/java/com/edu/espp/config/PasswordEncoderConfig.java#L17):

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Spring Security dùng bean này để đối chiếu:

```text
mật khẩu thô người dùng nhập
        ↓ BCryptPasswordEncoder.matches(...)
password_hash lấy từ database
```

Mật khẩu thô không được lưu xuống database và không cần mã hóa lại rồi so sánh chuỗi trực tiếp, vì BCrypt tạo salt khác nhau.

## 9. Khi đăng nhập thành công

Nếu email, trạng thái và mật khẩu đều hợp lệ, Spring Security:

1. Tạo đối tượng `Authentication` đã xác thực.
2. Đưa Authentication vào `SecurityContext`.
3. Lưu SecurityContext trong HTTP session.
4. Gửi cookie session `JSESSIONID` về trình duyệt.
5. Chạy success handler.

Success handler nằm tại [SecurityConfig.java (line 63)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L63).

### 9.1. Kiểm tra role

Handler đọc authorities từ `authentication`:

- [Kiểm tra ROLE_ADMIN (line 65)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L65)
- [Kiểm tra ROLE_STAFF (line 71)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L71)

### 9.2. Redirect theo role

Logic chuyển hướng nằm tại [SecurityConfig.java (line 77)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L77):

```java
if (isAdmin) {
    response.sendRedirect("/admin/dashboard");
} else if (isStaff) {
    response.sendRedirect("/staff/dashboard");
} else {
    response.sendRedirect("/dashboard");
}
```

Kết quả:

| Role | Trang sau đăng nhập |
|---|---|
| `ADMIN` | `/admin/dashboard` |
| `STAFF` | `/staff/dashboard` |
| `STUDENT` | `/dashboard` |

## 10. Đưa User entity vào Model và session

Ở request tiếp theo sau redirect, [GlobalControllerAdvice.populateUser() (line 29)](../src/main/java/com/edu/espp/common/advise/GlobalControllerAdvice.java#L29) chạy trước các controller.

Hàm đọc Authentication tại [GlobalControllerAdvice.java (line 31)](../src/main/java/com/edu/espp/common/advise/GlobalControllerAdvice.java#L31):

```java
Authentication auth = SecurityContextHolder
        .getContext()
        .getAuthentication();
```

Sau đó dùng email trong principal để tìm lại User:

```java
Optional<User> maybeUser = userRepository.findByEmail(email);
```

Nếu tìm thấy, User được lưu vào session tại [GlobalControllerAdvice.java (line 36)](../src/main/java/com/edu/espp/common/advise/GlobalControllerAdvice.java#L36):

```java
session.setAttribute("currentUser", maybeUser.get());
```

Đồng thời User được trả về dưới tên model attribute `user`, để Thymeleaf có thể dùng:

```html
${user.email}
${user.fullName}
${user.role}
```

## 11. Khi đăng nhập thất bại

Nếu email không tồn tại, tài khoản không active hoặc mật khẩu sai, Spring Security thực hiện cấu hình tại [SecurityConfig.java (line 87)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L87):

```java
.failureUrl("/login?error")
```

Luồng thất bại:

```text
Xác thực thất bại
        ↓
Redirect GET /login?error
        ↓
LoginController.showLoginPage(error)
        ↓
model.addAttribute("error", "Email hoặc mật khẩu không đúng")
        ↓
auth/login.html hiển thị banner lỗi
```

Thông báo lỗi được tạo tại [LoginController.java (line 28)](../src/main/java/com/edu/espp/controller/auth/LoginController.java#L28) và hiển thị trong [auth/login.html (line 57)](../src/main/resources/templates/auth/login.html#L57).

## 12. Phân quyền sau đăng nhập

Các quy tắc URL nằm trong [SecurityConfig.securityFilterChain()](../src/main/java/com/edu/espp/config/SecurityConfig.java#L12):

- `/admin/**`: chỉ `ADMIN`.
- `/staff/**`: `STAFF` hoặc `ADMIN`.
- `/student/**`, `/dashboard`, `/histories/**`: chỉ `STUDENT`.
- `/exams/**`: `ADMIN`, `STAFF` hoặc `STUDENT`.
- URL còn lại: phải đăng nhập.

Nếu đã đăng nhập nhưng truy cập URL không đúng role, Spring Security chặn request trước khi controller chạy.

## 13. Những trường hiện chưa được cập nhật khi login

[User.java](../src/main/java/com/edu/espp/entity/User.java) có các trường:

- [loginAttempts (line 41)](../src/main/java/com/edu/espp/entity/User.java#L41)
- [lockedUntil (line 45)](../src/main/java/com/edu/espp/entity/User.java#L45)
- [lastLoginAt (line 48)](../src/main/java/com/edu/espp/entity/User.java#L48)
- [lastLoginIp (line 51)](../src/main/java/com/edu/espp/entity/User.java#L51)

Tuy nhiên luồng đăng nhập hiện tại chưa có success handler/failure handler riêng để cập nhật các trường này. Vì vậy hiện tại:

- Đăng nhập sai không tăng `loginAttempts`.
- Chưa tự khóa tài khoản bằng `lockedUntil`.
- Đăng nhập thành công không cập nhật `lastLoginAt`.
- Đăng nhập thành công không cập nhật `lastLoginIp`.
- `failureUrl("/login?error")` gom các lỗi về cùng một thông báo chung.

## 14. Những file chính trong workflow

### Frontend

- [auth/login.html](../src/main/resources/templates/auth/login.html)
- [auth/fragments/password-input.html](../src/main/resources/templates/auth/fragments/password-input.html)

### Controller và Security

- [LoginController.java](../src/main/java/com/edu/espp/controller/auth/LoginController.java)
- [SecurityConfig.java](../src/main/java/com/edu/espp/config/SecurityConfig.java)
- [GlobalControllerAdvice.java](../src/main/java/com/edu/espp/common/advise/GlobalControllerAdvice.java)

### Service, Repository và Entity

- [StudentDetailsService.java](../src/main/java/com/edu/espp/service/auth/StudentDetailsService.java)
- [UserRepository.java](../src/main/java/com/edu/espp/repository/UserRepository.java)
- [User.java](../src/main/java/com/edu/espp/entity/User.java)
- [PasswordEncoderConfig.java](../src/main/java/com/edu/espp/config/PasswordEncoderConfig.java)

## 15. Tóm tắt

```text
GET /login
    ↓ LoginController.showLoginPage()
auth/login.html
    ↓ POST /login: email + password + CSRF
Spring Security filter
    ↓ StudentDetailsService.loadUserByUsername()
UserRepository.findByEmail()
    ↓ User entity từ database
Kiểm tra ACTIVE
    ↓ BCrypt kiểm tra password
Tạo Authentication + session + JSESSIONID
    ↓ successHandler đọc ROLE
Redirect dashboard tương ứng
    ↓ request dashboard tiếp theo
GlobalControllerAdvice.populateUser()
    ↓
Đưa User vào model và currentUser vào session
```
