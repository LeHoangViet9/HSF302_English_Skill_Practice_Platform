# Workflow đăng xuất

Tài liệu mô tả luồng từ lúc người dùng nhấn nút Đăng xuất trên frontend đến khi Spring Security xóa trạng thái đăng nhập và chuyển về trang login.

## 1. Tổng quan

```text
Người dùng nhấn Đăng xuất
    ↓
POST /logout + CSRF token
    ↓
Spring Security LogoutFilter
    ↓
Xóa Authentication khỏi SecurityContext
    ↓
Hủy HTTP session
    ↓
Xóa cookie JSESSIONID
    ↓
Redirect /login?logout
    ↓
LoginController.showLoginPage()
    ↓
Hiển thị “Bạn đã đăng xuất thành công.”
```

## 2. Nút logout trên frontend

Form nằm tại [fragments/layout.html (line 199)](../src/main/resources/templates/fragments/layout.html#L199):

```html
<form th:action="@{/logout}" method="post">
    <button type="submit">Đăng xuất</button>
</form>
```

Thymeleaf/Spring Security tự bổ sung CSRF token vào form POST. Khi nhấn nút, trình duyệt gửi:

```http
POST /logout
Cookie: JSESSIONID=<session hiện tại>
```

Không có `LogoutController` riêng; request được Spring Security xử lý.

## 3. SecurityConfig xử lý logout

Luồng được cấu hình tại [SecurityConfig.java (line 93)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L93):

```java
.logout(logout -> logout
        .logoutUrl("/logout")
        .logoutSuccessUrl("/login?logout")
        .invalidateHttpSession(true)
        .clearAuthentication(true)
        .deleteCookies("JSESSIONID")
        .permitAll())
```

Các bước:

- [`logoutUrl("/logout")` (line 96)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L96): Spring Security bắt POST.
- [`invalidateHttpSession(true)` (line 102)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L102): hủy session server-side.
- [`clearAuthentication(true)` (line 105)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L105): xóa principal/authorities khỏi SecurityContext.
- [`deleteCookies("JSESSIONID")` (line 108)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L108): yêu cầu trình duyệt xóa cookie session.
- [`logoutSuccessUrl("/login?logout")` (line 99)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L99): chuyển về login.

## 4. Hiển thị thông báo

Sau redirect, [LoginController.showLoginPage() (line 16)](../src/main/java/com/edu/espp/controller/auth/LoginController.java#L16) nhận parameter `logout`.

[LoginController.java (line 36)](../src/main/java/com/edu/espp/controller/auth/LoginController.java#L36) đặt:

```java
model.addAttribute(
    "successMsg",
    "Bạn đã đăng xuất thành công."
);
```

[auth/login.html](../src/main/resources/templates/auth/login.html) hiển thị `successMsg`.

## 5. Trạng thái sau logout

```text
SecurityContext: không còn Authentication đã xác thực
HTTP session cũ: invalidated
JSESSIONID cũ: bị xóa phía trình duyệt
currentUser trong session cũ: mất theo session
```

Khi truy cập URL yêu cầu đăng nhập, Spring Security chuyển người dùng về `/login`.

## 6. File chính

- [fragments/layout.html](../src/main/resources/templates/fragments/layout.html)
- [SecurityConfig.java](../src/main/java/com/edu/espp/config/SecurityConfig.java)
- [LoginController.java](../src/main/java/com/edu/espp/controller/auth/LoginController.java)
- [auth/login.html](../src/main/resources/templates/auth/login.html)

## 7. Tóm tắt

```text
Frontend POST /logout
    ↓ SecurityConfig / LogoutFilter
clearAuthentication
    ↓
invalidateHttpSession
    ↓
delete JSESSIONID
    ↓
/login?logout
    ↓ LoginController
Thông báo đăng xuất thành công
```
